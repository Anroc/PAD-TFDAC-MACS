package de.tuberlin.tfdacmacs.lib.attributes.data;

import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Data
@ToString
@NoArgsConstructor
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

    public static String generateId(@NonNull Attribute attribute, @NonNull Object value) {
        return attribute.getId() + ":" + value.toString();
    }

    public static String generateId(@NonNull String aid, @NonNull String attributeName, @NonNull Object value) {
        return aid + "." + attributeName + ":" + value.toString();
    }
}