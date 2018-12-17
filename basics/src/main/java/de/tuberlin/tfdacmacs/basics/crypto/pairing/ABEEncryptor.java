package de.tuberlin.tfdacmacs.basics.crypto.pairing;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.*;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.AsymmetricElementKey;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.AuthorityKey;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.CipherTextUpdateKey;
import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ABEEncryptor extends ABECrypto {

    public CipherTextDescription encrypt(
            @NonNull AndAccessPolicy andAccessPolicy,
            @NonNull GlobalPublicParameter gpp,
            DataOwner dataOwner) {
        if(andAccessPolicy.getAccessPolicyElements() == null || andAccessPolicy.getAccessPolicyElements().isEmpty()) {
            throw new IllegalArgumentException("No Attribute policy given.");
        }

        Set<String> attributeValueIdentifier = andAccessPolicy.getAccessPolicyElements().stream()
                .map(AccessPolicyElement::getAttributeValueId)
                .collect(Collectors.toSet());

        Element key = gpp.getPairing().getGT().newRandomElement().getImmutable();
        Element s = gpp.getPairing().getZr().newRandomElement().getImmutable();

        Map<AuthorityKey.Public, Set<AccessPolicyElement>> policy = andAccessPolicy.groupByAttributeAuthority();

        Element c1 = mulAuthorityPublicKeys(policy);
        Element c2 = gpp.getG().powZn(s).getImmutable();
        Element c3 = mulAttributePublicValueKeys(andAccessPolicy.getAccessPolicyElements());

        c1 = key.duplicate().mul(c1.duplicate().powZn(s)).getImmutable();

        if(dataOwner == null) {
            c3 = c3.powZn(s).getImmutable();
            return new CipherTextDescription(c1, c2, c3, attributeValueIdentifier, null, key);
        } else {
            c3 = c3.powZn(s.duplicate().add(dataOwner.getTwoFactorPrivateKey().getKey())).getImmutable();
            return new CipherTextDescription(c1, c2, c3, attributeValueIdentifier, dataOwner.getId(), null, key);
        }
    }

    public CipherText update(
            @NonNull GlobalPublicParameter gpp,
            @NonNull CipherText cipherText,
            @NonNull AndAccessPolicy andAccessPolicy,
            @NonNull CipherTextUpdateKey cipherTextUpdateKey) {
        if(! cipherText.getAccessPolicy().contains(cipherTextUpdateKey.getAttributeValueId())
            || ! andAccessPolicy.contains(cipherTextUpdateKey.getAttributeValueId())) {

            log.info("Nothing to do on cipher text. Policy does not contain the attribute value to update.");
            return cipherText;
        }

        Element r = gpp.getPairing().getZr().newRandomElement();
        Element updatedC1 = cipherText.getC1().duplicate().mul(
                mulAuthorityPublicKeys(andAccessPolicy.groupByAttributeAuthority()).powZn(r));
        Element updatedC2 = cipherText.getC2().duplicate().mul(gpp.getG().powZn(r));
        Element updatedC3 = cipherText.getC3().duplicate().mul(cipherTextUpdateKey.getUpdateKey())
                .mul(mulAttributePublicValueKeys(
                        andAccessPolicy.getAccessPolicyElements(), cipherTextUpdateKey.getAttributeValueId())
                        .orElse(gpp.getPairing().getG1().newOneElement())
                        .powZn(r))
                .mul(cipherTextUpdateKey.getNewAttributeValuePublicKey().getKey().duplicate().powZn(r));

        return new CipherText(updatedC1, updatedC2, updatedC3, cipherText.getAccessPolicy(), cipherText.getOwnerId(), cipherText.getEncryptedMessage());
    }

    private Element mulAuthorityPublicKeys(Map<AsymmetricElementKey.Public, Set<AccessPolicyElement>> policy) {
        Element c1 = null;
        for(Map.Entry<AuthorityKey.Public, Set<AccessPolicyElement>> entry : policy.entrySet()) {
            Element authorityPublicKey = entry.getKey().getKey().duplicate();
            int n = entry.getValue().size();

            c1 = mulOrDefault(c1, authorityPublicKey.pow(BigInteger.valueOf(n)));
        }
        return c1;
    }

    private Element mulAttributePublicValueKeys(@NonNull Set<AccessPolicyElement> accessPolicyElements) {
        return mulAttributePublicValueKeys(accessPolicyElements, null).get();
    }

    private Optional<Element> mulAttributePublicValueKeys(
            @NonNull Set<AccessPolicyElement> policy, String excludedAttributeValueId) {
        return policy.stream()
                .filter(accessPolicyElement -> ! accessPolicyElement.getAttributeValueId().equals(excludedAttributeValueId))
                .map(AccessPolicyElement::getAttributePublicKey)
                .map(AttributeValueKey.Public::getKey)
                .reduce((a, b) -> a.duplicate().mul(b));
    }
}
