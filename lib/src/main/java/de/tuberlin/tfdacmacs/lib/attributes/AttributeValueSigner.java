package de.tuberlin.tfdacmacs.lib.attributes;

import de.tuberlin.tfdacmacs.lib.attributes.data.AttributeValueComponent;

public interface AttributeValueSigner {

    String sign(AttributeValueComponent value);
}
