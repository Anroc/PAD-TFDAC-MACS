package de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class AttributeValueKey extends AsymmetricElementKey<AttributeValueKey> {

    private final String attributeValueId;

    public AttributeValueKey(@NonNull Element privateKey, @NonNull Element publicKey, @NonNull String attributeValueId) {
        super(new AttributeValueKey.Private(privateKey, attributeValueId), new AttributeValueKey.Public(publicKey, attributeValueId));
        this.attributeValueId = attributeValueId;
    }

    @Override
    public AttributeValueKey.Private<AttributeValueKey> getPrivateKey() {
        return (AttributeValueKey.Private<AttributeValueKey>) super.getPrivateKey();
    }

    @Override
    public AttributeValueKey.Public<AttributeValueKey> getPublicKey() {
        return (AttributeValueKey.Public<AttributeValueKey>) super.getPublicKey();
    }

    @Data
    @SuppressWarnings("unused")
    public static class Private<T> extends AsymmetricElementKey.Private<AttributeValueKey> {
        private final String attributeValueId;

        public Private(@NonNull Element key, @NonNull String attributeValueId) {
            super(key);
            this.attributeValueId = attributeValueId;
        }
    }

    @Data
    @SuppressWarnings("unused")
    public static class Public<T> extends AsymmetricElementKey.Public<AttributeValueKey> {
        private final String attributeValueId;

        public Public(@NonNull Element key, @NonNull String attributeValueId) {
            super(key);
            this.attributeValueId = attributeValueId;
        }
    }
}
