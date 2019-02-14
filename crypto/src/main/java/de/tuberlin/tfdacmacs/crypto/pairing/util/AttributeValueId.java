package de.tuberlin.tfdacmacs.crypto.pairing.util;

import lombok.Data;
import lombok.NonNull;

@Data
public class AttributeValueId {

    private final String authorityId;
    private final String value;
    private final String attributeId;
    private final String attributeValueId;

    public AttributeValueId(@NonNull String attributeValueId) {
        String[] split = attributeValueId.split(":");
        if(split.length != 2) {
            throw new IllegalArgumentException("AttributeValueId was malformed [does contain to many or non ':']: "
                    + attributeValueId);
        }

        this.attributeId = split[0];
        this.value = split[1];

        int index = attributeValueId.lastIndexOf('.');
        if(index <= 0) {
            throw  new IllegalArgumentException("AttributeId was malformed [does not contain '.']: " + attributeValueId);
        }
        this.authorityId = attributeValueId.substring(0, index);
        this.attributeValueId = attributeValueId;
    }
}
