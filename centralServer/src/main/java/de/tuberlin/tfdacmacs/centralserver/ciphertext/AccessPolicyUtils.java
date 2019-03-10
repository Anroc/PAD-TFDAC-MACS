package de.tuberlin.tfdacmacs.centralserver.ciphertext;

import de.tuberlin.tfdacmacs.centralserver.attribute.PublicAttributeService;
import de.tuberlin.tfdacmacs.centralserver.authority.AttributeAuthorityService;
import de.tuberlin.tfdacmacs.crypto.pairing.data.AndAccessPolicy;
import de.tuberlin.tfdacmacs.crypto.pairing.data.AttributePolicyElement;
import de.tuberlin.tfdacmacs.crypto.pairing.data.VersionedID;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AsymmetricElementKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.pairing.util.AttributeValueId;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class AccessPolicyUtils {

    private final AttributeAuthorityService attributeAuthorityService;
    private final PublicAttributeService publicAttributeService;

    public AndAccessPolicy buildAccessPolicy(@NonNull Set<VersionedID> accessPolicy) {
        AndAccessPolicy andAccessPolicy = new AndAccessPolicy();

        accessPolicy.stream()
                .map(this::buildAccessPolicyElement)
                .forEach(andAccessPolicy::put);

        return andAccessPolicy;

    }

    public AttributePolicyElement buildAccessPolicyElement(@NonNull VersionedID attributeValueId) {
        AttributeValueId attrValueId = new AttributeValueId(attributeValueId.getId());

        AttributeValueKey.Public attributeValuePublicKey = publicAttributeService.findEntity(attrValueId.getAttributeId())
                .orElseThrow(() -> new IllegalStateException(
                        "Could not find attribute public key for attribute: " + attrValueId.getAttributeId()))
                .getValues()
                .stream()
                .filter(value -> value.getValue().equals(attrValueId.getValue()))
                .findAny()
                .orElseThrow(() -> new IllegalStateException(
                        "Could not find attribute value public key for attribute value: " + attrValueId
                                .getAttributeValueId()))
                .toAttributeValuePublicKey(attributeValueId.getId());

        AsymmetricElementKey.Public authorityPublicKey = attributeAuthorityService.findEntity(attrValueId.getAuthorityId())
                .orElseThrow(() -> new IllegalStateException(
                        "Could not find Authority public key for authority: " + attrValueId.getAuthorityId()))
                .getPublicKey();

        return new AttributePolicyElement(
                authorityPublicKey,
                attributeValuePublicKey,
                new VersionedID(attributeValueId.getId(), attributeValuePublicKey.getVersion())
        );
    }
}
