package de.tuberlin.tfdacmacs.basics.crypto.pairing;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.UserAttributeSecretComponents;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.CipherText;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.TwoFactorKey;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.UserAttributeValueKey;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.exceptions.AccessPolicyNotSatisfiedException;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.util.HashGenerator;
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
            @NonNull String userId, @NonNull Set<UserAttributeSecretComponents> secrets,
            TwoFactorKey.Public twoFactorPublicKey) {
        secrets = findSatisfingSubSet(cipherText, secrets);


        Element sk = secrets.stream()
                .map(UserAttributeSecretComponents::getUserSecretAttributeKey)
                .map(UserAttributeValueKey::getSecretKey)
                .reduce((a,b) -> a.duplicate().mul(b))
                .orElseThrow(() -> new IllegalArgumentException("Given secrets where empty."));

        Element upk = secrets.stream()
                .map(UserAttributeSecretComponents::getAttributePublicKey)
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

    private Set<UserAttributeSecretComponents> findSatisfingSubSet(CipherText cipherText, Set<UserAttributeSecretComponents> secrets) {
        Set<UserAttributeSecretComponents> satisfyingSubSet = getSatisfyingSubSet(cipherText.getAccessPolicy(), secrets);
        if(satisfyingSubSet.size() != cipherText.getAccessPolicy().size()) {
            throw new AccessPolicyNotSatisfiedException(
                    String.format("Policy %s could not be fulfilled. Missing %d attribute keys.",
                            cipherText.getAccessPolicy(),
                            cipherText.getAccessPolicy().size() - satisfyingSubSet.size()
                    )
            );
        }

        return satisfyingSubSet;
    }

    private Set<UserAttributeSecretComponents> getSatisfyingSubSet(Set<String> accessPolicy, Set<UserAttributeSecretComponents> secrets) {
        return secrets.stream()
                .filter(secret -> accessPolicy.contains(secret.getAttributeValueId()))
                .collect(Collectors.toSet());
    }
}
