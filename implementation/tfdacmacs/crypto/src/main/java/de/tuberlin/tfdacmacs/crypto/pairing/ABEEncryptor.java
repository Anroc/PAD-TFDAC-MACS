package de.tuberlin.tfdacmacs.crypto.pairing;

import com.google.common.collect.Sets;
import de.tuberlin.tfdacmacs.crypto.pairing.data.*;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.*;
import de.tuberlin.tfdacmacs.crypto.pairing.exceptions.VersionMismatchException;
import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.HashSet;
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
            DataOwner dataOwner,
            Element key) {
        if(andAccessPolicy.getAttributePolicyElements() == null || andAccessPolicy.getAttributePolicyElements().isEmpty()) {
            throw new IllegalArgumentException("No Attribute policy given.");
        }

        Set<VersionedID> attributeValueIdentifier = andAccessPolicy.getAttributePolicyElements().stream()
                .map(AttributePolicyElement::getAttributeValueId)
                .collect(Collectors.toSet());

        key = (key != null)? key : gpp.gt().newRandomElement().getImmutable();
        Element s = gpp.zr().newRandomElement().getImmutable();

        Map<AuthorityKey.Public, Set<AttributePolicyElement>> policy = andAccessPolicy.groupByAttributeAuthority();

        Element c1 = mulAuthorityPublicKeys(policy);
        Element c2 = gpp.getG().powZn(s).getImmutable();
        Element c3 = mulAttributePublicValueKeys(andAccessPolicy.getAttributePolicyElements());

        c1 = key.duplicate().mul(c1.duplicate().powZn(s)).getImmutable();

        if(dataOwner == null) {
            c3 = c3.powZn(s).getImmutable();
            return new CipherTextDescription(c1, c2, c3, attributeValueIdentifier, null, key);
        } else {
            c3 = c3.powZn(s.duplicate().add(dataOwner.getTwoFactorPrivateKey().getKey())).getImmutable();
            return new CipherTextDescription(c1, c2, c3, attributeValueIdentifier, dataOwner.getId(), key);
        }
    }

    public CipherText update(
            @NonNull GlobalPublicParameter gpp,
            @NonNull CipherText cipherText,
            @NonNull AndAccessPolicy andAccessPolicy,
            @NonNull CipherTextAttributeUpdateKey cipherTextAttributeUpdateKey) {
        if(! contains(cipherText.getAccessPolicy(), cipherTextAttributeUpdateKey.getAttributeValueId(), false)) {
            log.warn("Nothing to do on cipher text. Policy does not contain the attribute value to update.");
        }

        checkConstrains(cipherText, andAccessPolicy, cipherTextAttributeUpdateKey, cipherTextAttributeUpdateKey.getAttributeValueId());

        Element r = gpp.zr().newRandomElement();
        Element updatedC1 = updateC1(cipherText, andAccessPolicy, r);
        Element updatedC2 = updateC2(gpp, cipherText, r);
        Element updatedC3 = cipherText.getC3().duplicate().mul(cipherTextAttributeUpdateKey.getUpdateKey())
                .mul(mulAttributePublicValueKeys(
                        andAccessPolicy.getAttributePolicyElements(), cipherTextAttributeUpdateKey.getAttributeValueId().getId())
                        .orElse(gpp.g1().newOneElement())
                        .powZn(r))
                .mul(cipherTextAttributeUpdateKey.getNewAttributeValuePublicKey().getKey().duplicate().powZn(r));

        HashSet accessPolicy = new HashSet(cipherText.getAccessPolicy());
        accessPolicy.remove(cipherTextAttributeUpdateKey.getAttributeValueId());
        accessPolicy.add(cipherTextAttributeUpdateKey.getAttributeValueId().increment());

        return new CipherText(cipherText.getId(), updatedC1, updatedC2, updatedC3, accessPolicy, cipherText.getOwnerId(), cipherText.getFileId());
    }

    private void checkConstrains(@NonNull CipherText cipherText, @NonNull AndAccessPolicy andAccessPolicy,
            @NonNull CipherTextAttributeUpdateKey cipherTextAttributeUpdateKey,
            @NonNull VersionedID attributeValueIdToIgnore) {
        if(! symmetric(andAccessPolicy.getAttributeValueIds(), cipherText.getAccessPolicy(), true, attributeValueIdToIgnore.getId())) {
            if (!symmetric(andAccessPolicy.getAttributeValueIds(), cipherText.getAccessPolicy(), false, attributeValueIdToIgnore.getId())) {
                throw new IllegalArgumentException("Given and access Policy does not mirror the policy in ciphertext");
            } else {
                throw new VersionMismatchException(
                        String.format("Given AndAccessPolicy %s is not the same as the cipher text policy %s",
                                andAccessPolicy.getAttributeValueIds(), cipherText.getAccessPolicy()));
            }
        }

        if(cipherText.isTwoFactorSecured() && ! cipherText.getOwnerId().equals(cipherTextAttributeUpdateKey.getDataOwnerId())) {
            throw new VersionMismatchException(
                    String.format("Given cipher text 2FA key has version %s but update key version was %s", cipherText.getOwnerId(), cipherTextAttributeUpdateKey.getDataOwnerId())
            );
        }
    }

    public CipherText update(
            @NonNull GlobalPublicParameter gpp,
            @NonNull CipherText cipherText,
            @NonNull AndAccessPolicy andAccessPolicy,
            @NonNull Set<CipherText2FAUpdateKey> cipherText2FAUpdateKeys) {
        if(! cipherText.isTwoFactorSecured()) {
            log.warn("Given cipher text to 2FA update was not a 2FA secured cipher text.");
            return cipherText;
        }

        checkConstrains(cipherText, cipherText2FAUpdateKeys);

        Element product = findSatisfingSubSet(cipherText, cipherText2FAUpdateKeys, CipherText2FAUpdateKey::getAttributeValueId)
                .stream().map(UpdateKey::getUpdateKey).reduce((a,b) -> a.duplicate().mul(b)).get();

        Element r = gpp.zr().newRandomElement();
        Element updatedC1 = updateC1(cipherText, andAccessPolicy, r);
        Element updatedC2 = updateC2(gpp, cipherText, r);
        Element updatedC3 = cipherText.getC3().duplicate()
                .mul(product)
                .mul(mulAttributePublicValueKeys(andAccessPolicy.getAttributePolicyElements()).powZn(r));
        return new CipherText(cipherText.getId(), updatedC1, updatedC2, updatedC3, cipherText.getAccessPolicy(), cipherText.getOwnerId().increment(), cipherText.getFileId());
    }

    private void checkConstrains(CipherText cipherText, Set<CipherText2FAUpdateKey> cipherText2FAUpdateKeys) {
        if (cipherText2FAUpdateKeys.isEmpty()) {
            throw new IllegalArgumentException("Given update key set is empty.");
        }

        cipherText2FAUpdateKeys.stream()
                .map(CipherText2FAUpdateKey::getOid)
                .forEach(oid -> {
                    if(! cipherText.getOwnerId().equals(oid)) {
                        if(! cipherText.getOwnerId().getId().equals(oid.getId())) {
                            throw new IllegalArgumentException("Given cipher text owner id does not match issued update key.");
                        } else {
                            throw new VersionMismatchException(cipherText.getOwnerId(), oid);
                        }
                    }
                });
    }

    private Element updateC1(@NonNull CipherText cipherText, @NonNull AndAccessPolicy andAccessPolicy, Element r) {
        return cipherText.getC1().duplicate().mul(
                mulAuthorityPublicKeys(andAccessPolicy.groupByAttributeAuthority()).powZn(r));
    }

    private Element updateC2(@NonNull GlobalPublicParameter gpp, @NonNull CipherText cipherText, Element r) {
        return cipherText.getC2().duplicate().mul(gpp.getG().powZn(r));
    }

    private Element mulAuthorityPublicKeys(Map<AsymmetricElementKey.Public, Set<AttributePolicyElement>> policy) {
        Element c1 = null;
        for(Map.Entry<AuthorityKey.Public, Set<AttributePolicyElement>> entry : policy.entrySet()) {
            Element authorityPublicKey = entry.getKey().getKey().duplicate();
            int n = entry.getValue().size();

            c1 = mulOrDefault(c1, authorityPublicKey.pow(BigInteger.valueOf(n)));
        }
        return c1;
    }

    private Element mulAttributePublicValueKeys(@NonNull Set<AttributePolicyElement> attributePolicyElements) {
        return mulAttributePublicValueKeys(attributePolicyElements, null).get();
    }

    private Optional<Element> mulAttributePublicValueKeys(
            @NonNull Set<AttributePolicyElement> policy,
            String excludedAttributeValueId) {
        return policy.stream()
                .filter(accessPolicyElement -> ! accessPolicyElement.getAttributeValueId().getId().equals(excludedAttributeValueId))
                .map(AttributePolicyElement::getAttributePublicKey)
                .map(AttributeValueKey.Public::getKey)
                .reduce((a, b) -> a.duplicate().mul(b))
                .map(Element::duplicate);
    }

    private boolean contains(Set<VersionedID> a, VersionedID b, boolean exact) {
        return containsAll(a, Sets.newHashSet(b), exact, null);
    }

    private boolean symmetric(Set<VersionedID> a, Set<VersionedID> b, boolean exact) {
        return containsAll(a, b, exact, null) && containsAll(b,a, exact, null);
    }

    private boolean symmetric(Set<VersionedID> a, Set<VersionedID> b, boolean exact, String ignoringAttributeValueId) {
        return containsAll(a, b, exact, ignoringAttributeValueId) && containsAll(b,a, exact, ignoringAttributeValueId);
    }

    private boolean containsAll(Set<VersionedID> a, Set<VersionedID> b, boolean exact, String ignoringAttributeValueId) {
        if(exact) {
            if(ignoringAttributeValueId != null) {
                a = new HashSet<>(a);
                b = new HashSet<>(b);

                a.removeIf(elem -> elem.getId().equals(ignoringAttributeValueId));
                b.removeIf(elem -> elem.getId().equals(ignoringAttributeValueId));
            }

            return a.containsAll(b);
        } else {
            Set<String> aIds = a.stream().map(VersionedID::getId)
                    .filter(elem -> ignoringAttributeValueId == null || ! elem.equals(ignoringAttributeValueId))
                    .collect(Collectors.toSet());

            return b.stream()
                    .map(VersionedID::getId)
                    .filter(elem -> ignoringAttributeValueId == null || ! elem.equals(ignoringAttributeValueId))
                    .allMatch(versionedID -> aIds.contains(versionedID));
        }
    }
}
