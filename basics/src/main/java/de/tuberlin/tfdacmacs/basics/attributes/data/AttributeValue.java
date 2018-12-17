package de.tuberlin.tfdacmacs.basics.attributes.data;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.AttributeValueKey;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotNull;

@Data
public class AttributeValue<T> extends AttributeValueKey {

    @NotNull
    private T value;

    public AttributeValue(@NonNull T value, @NonNull AttributeValueKey attributeValueKey) {
        super(attributeValueKey.getPrivateKey().getKey(), attributeValueKey.getPublicKey().getKey(), attributeValueKey.getAttributeValueId());
        this.value = value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
