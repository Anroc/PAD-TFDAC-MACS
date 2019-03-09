package de.tuberlin.tfdacmacs.crypto.pairing;

import de.tuberlin.tfdacmacs.crypto.pairing.data.CipherText;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.UserAttributeSecretComponent;
import de.tuberlin.tfdacmacs.crypto.pairing.data.VersionedID;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.UserAttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.pairing.exceptions.AccessPolicyNotSatisfiedException;
import de.tuberlin.tfdacmacs.crypto.pairing.exceptions.TwoFactorContrainNotStatisfiedException;
import de.tuberlin.tfdacmacs.crypto.pairing.exceptions.VersionMismatchException;
import de.tuberlin.tfdacmacs.crypto.pairing.util.HashGenerator;
import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ABEDecryptor extends ABECrypto {

    private final HashGenerator hashGenerator;

    public Element decrypt(@NonNull CipherText cipherText, @NonNull GlobalPublicParameter gpp,
            @NonNull String userId, @NonNull Set<UserAttributeSecretComponent> secrets,
            TwoFactorKey.Public twoFactorPublicKey) {
        if(twoFactorPublicKey == null && cipherText.isTwoFactorSecured()) {
            throw new TwoFactorContrainNotStatisfiedException(
                    "Cipher text is two-factor secured but no 2FA decryption key was given.");
        }

        if(cipherText.isTwoFactorSecured() && twoFactorPublicKey.getVersion() != cipherText.getOwnerId().getVersion()) {
            throw new VersionMismatchException(cipherText.getOwnerId(), twoFactorPublicKey);
        }

        secrets = findSatisfingSubSet(cipherText, secrets);


        Element sk = secrets.stream()
                .map(UserAttributeSecretComponent::getUserSecretAttributeKey)
                .map(UserAttributeValueKey::getKey)
                .reduce((a,b) -> a.duplicate().mul(b))
                .orElseThrow(() -> new IllegalArgumentException("Given secrets where empty."));

        Element upk = secrets.stream()
                .map(UserAttributeSecretComponent::getAttributePublicKey)
                .map(AttributeValueKey.Public::getKey)
                .reduce((a,b) -> a.duplicate().mul(b))
                .orElseThrow(() -> new IllegalArgumentException("Given secrets where empty."));

        Element pairing1 = gpp.getPairing().pairing(hashGenerator.g1Hash(gpp, userId), cipherText.getC3());
        Element upper = cipherText.getC1().duplicate().mul(pairing1);

        Element pairing2 = gpp.getPairing().pairing(cipherText.getC2(), sk);

        if(twoFactorPublicKey == null) {
            return upper.div(pairing2);
        } else {
            Element pairing3 = gpp.getPairing().pairing(twoFactorPublicKey.getKey(), upk);
            Element lower = pairing2.mul(pairing3);
            return upper.div(lower);
        }
    }

    private Set<UserAttributeSecretComponent> findSatisfingSubSet(CipherText cipherText, Set<UserAttributeSecretComponent> secrets) {
        Set<UserAttributeSecretComponent> satisfyingSubSet = getSatisfyingSubSet(cipherText.getAccessPolicy(), secrets, true);
        if(satisfyingSubSet.size() != cipherText.getAccessPolicy().size()) {
            satisfyingSubSet = getSatisfyingSubSet(cipherText.getAccessPolicy(), secrets, false);


            if(satisfyingSubSet.size() == cipherText.getAccessPolicy().size()) {
                String message = String.format("Policy %s could not be fulfilled by %s. Version mismatch.",
                        cipherText.getAccessPolicy(),
                        secrets.stream().map(UserAttributeSecretComponent::getAttributeValueId).collect(Collectors.toSet())
                );
                throw new VersionMismatchException(message);
            } else {
                String message = String.format("Policy %s could not be fulfilled by %s. Missing %d attribute keys.",
                        cipherText.getAccessPolicy(),
                        secrets.stream().map(UserAttributeSecretComponent::getAttributeValueId).collect(Collectors.toSet()),
                        cipherText.getAccessPolicy().size() - satisfyingSubSet.size()
                );
                throw new AccessPolicyNotSatisfiedException(message);
            }
        }

        return satisfyingSubSet;
    }

    private Set<UserAttributeSecretComponent> getSatisfyingSubSet(Set<VersionedID> accessPolicy, Set<UserAttributeSecretComponent> secrets, boolean exactMatch) {
        if(exactMatch) {
            return secrets.stream()
                    .filter(secret -> accessPolicy.contains(secret.getAttributeValueId()))
                    .collect(Collectors.toSet());
        } else {
            Set<String> collect = accessPolicy.stream().map(VersionedID::getId).collect(Collectors.toSet());

            return secrets.stream()
                    .filter(secret -> collect.contains(secret.getAttributeValueId().getId()))
                    .collect(Collectors.toSet());
        }
    }
}
