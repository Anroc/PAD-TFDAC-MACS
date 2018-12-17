package de.tuberlin.tfdacmacs.basics.crypto.pairing.data;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.AuthorityKey;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class AccessPolicyElement {

    private final AuthorityKey.Public authorityPublicKey;
    private final AttributeValueKey.Public attributePublicKey;

    private final String attributeValueId;

}
