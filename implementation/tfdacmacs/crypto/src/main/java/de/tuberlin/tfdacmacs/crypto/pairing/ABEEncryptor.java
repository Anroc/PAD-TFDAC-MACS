package de.tuberlin.tfdacmacs.crypto.pairing;

import de.tuberlin.tfdacmacs.crypto.pairing.data.*;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.*;
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
            @NonNull CipherTextAttributeUpdateKey cipherTextAttributeUpdateKey) {
        if(! cipherText.getAccessPolicy().contains(cipherTextAttributeUpdateKey.getAttributeValueId())
            || ! andAccessPolicy.contains(cipherTextAttributeUpdateKey.getAttributeValueId())) {

            log.info("Nothing to do on cipher text. Policy does not contain the attribute value to update.");
            return cipherText;
        }

        Element r = gpp.getPairing().getZr().newRandomElement();
        Element updatedC1 = updateC1(cipherText, andAccessPolicy, r);
        Element updatedC2 = updateC2(gpp, cipherText, r);
        Element updatedC3 = cipherText.getC3().duplicate().mul(cipherTextAttributeUpdateKey.getUpdateKey())
                .mul(mulAttributePublicValueKeys(
                        andAccessPolicy.getAccessPolicyElements(), cipherTextAttributeUpdateKey.getAttributeValueId())
                        .orElse(gpp.getPairing().getG1().newOneElement())
                        .powZn(r))
                .mul(cipherTextAttributeUpdateKey.getNewAttributeValuePublicKey().getKey().duplicate().powZn(r));

        return new CipherText(updatedC1, updatedC2, updatedC3, cipherText.getAccessPolicy(), cipherText.getOwnerId(), cipherText.getEncryptedMessage());
    }

    public CipherText update(
            @NonNull GlobalPublicParameter gpp,
            @NonNull CipherText cipherText,
            @NonNull AndAccessPolicy andAccessPolicy,
            @NonNull Set<CipherText2FAUpdateKey> cipherText2FAUpdateKeys) {
        if(! cipherText.isTwoFactorSecured()) {
            return cipherText;
        }
        if (cipherText2FAUpdateKeys.isEmpty()) {
            throw new IllegalArgumentException("Given update key set is empty.");
        }
        boolean allMatch = cipherText2FAUpdateKeys.stream()
                .map(CipherText2FAUpdateKey::getOid)
                .allMatch(oid -> cipherText.getOwnerId().equals(oid));
        if(! allMatch) {
            throw new IllegalArgumentException("Given ciphertext owner id does not match issued update key.");
        }

        Set<Element> subSet2FaUpdateKeys = cipherText2FAUpdateKeys.stream()
                .filter(cipherText2FAUpdateKey -> cipherText.getAccessPolicy()
                        .contains(cipherText2FAUpdateKey.getAttributeValueId()))
                .map(UpdateKey::getUpdateKey)
                .collect(Collectors.toSet());

        if(subSet2FaUpdateKeys.size() != cipherText.getAccessPolicy().size()) {
            throw new IllegalArgumentException(
                    String.format("Could not match all attribute keys to an update key. Missing %d update keys.",
                            cipherText.getAccessPolicy().size() - subSet2FaUpdateKeys.size()));
        }

        Element r = gpp.getPairing().getZr().newRandomElement();
        Element updatedC1 = updateC1(cipherText, andAccessPolicy, r);
        Element updatedC2 = updateC2(gpp, cipherText, r);
        Element updatedC3 = cipherText.getC3().duplicate()
                .mul(subSet2FaUpdateKeys.stream().reduce((a,b) -> a.duplicate().mul(b)).get())
                .mul(mulAttributePublicValueKeys(andAccessPolicy.getAccessPolicyElements()).powZn(r));
        return new CipherText(updatedC1, updatedC2, updatedC3, cipherText.getAccessPolicy(), cipherText.getOwnerId(), cipherText.getEncryptedMessage());
    }

    private Element updateC1(@NonNull CipherText cipherText, @NonNull AndAccessPolicy andAccessPolicy, Element r) {
        return cipherText.getC1().duplicate().mul(
                mulAuthorityPublicKeys(andAccessPolicy.groupByAttributeAuthority()).powZn(r));
    }

    private Element updateC2(@NonNull GlobalPublicParameter gpp, @NonNull CipherText cipherText, Element r) {
        return cipherText.getC2().duplicate().mul(gpp.getG().powZn(r));
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
                .reduce((a, b) -> a.duplicate().mul(b))
                .map(Element::duplicate);
    }
}
