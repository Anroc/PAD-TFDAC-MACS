package de.tuberlin.tfdacmacs.basics.crypto.pairing.data;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.UserAttributeValueKey;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UserAttributeSecretComponents {

    private final UserAttributeValueKey userSecretAttributeKey;
    private final AttributeValueKey.Public attributePublicKey;

    private final String attributeValueId;
}
