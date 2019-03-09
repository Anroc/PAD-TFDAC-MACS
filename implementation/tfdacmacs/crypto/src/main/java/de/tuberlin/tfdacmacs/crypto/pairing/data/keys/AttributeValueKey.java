package de.tuberlin.tfdacmacs.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AttributeValueKey extends AsymmetricElementKey<AttributeValueKey> {

    @NotNull
    private String attributeValueId;

    public AttributeValueKey(@NonNull Element privateKey, @NonNull Element publicKey, @NonNull String attributeValueId, long version) {
        super(
                new AttributeValueKey.Private(privateKey, attributeValueId, version),
                new AttributeValueKey.Public(publicKey, attributeValueId, version),
                version
        );
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
    @EqualsAndHashCode(callSuper = true)
    @SuppressWarnings("unused")
    public static class Private<T> extends AsymmetricElementKey.Private<AttributeValueKey> {
        private final String attributeValueId;

        public Private(@NonNull Element key, @NonNull String attributeValueId, long version) {
            super(key, version);
            this.attributeValueId = attributeValueId;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @SuppressWarnings("unused")
    public static class Public<T> extends AsymmetricElementKey.Public<AttributeValueKey> {
        private final String attributeValueId;

        public Public(@NonNull Element key, @NonNull String attributeValueId, long version) {
            super(key, version);
            this.attributeValueId = attributeValueId;
        }
    }
}
