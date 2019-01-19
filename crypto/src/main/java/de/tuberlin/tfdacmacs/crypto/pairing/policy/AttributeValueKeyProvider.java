package de.tuberlin.tfdacmacs.crypto.pairing.policy;

import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import lombok.NonNull;

public interface AttributeValueKeyProvider {

    AttributeValueKey.Public getAttributeValuePublicKey(@NonNull String attributeValueId);

}
