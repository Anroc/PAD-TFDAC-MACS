package de.tuberlin.tfdacmacs.crypto.pairing.data;

import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AuthorityKey;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AttributePolicyElement extends AccessPolicyElement {

    private final AuthorityKey.Public authorityPublicKey;
    private final AttributeValueKey.Public attributePublicKey;

    private final String attributeValueId;

    @Override
    public void put(@NonNull AccessPolicyElement accessPolicyElement) {
        throw new UnsupportedOperationException("Can not put accessPolicyElement on attribute");
    }
}
