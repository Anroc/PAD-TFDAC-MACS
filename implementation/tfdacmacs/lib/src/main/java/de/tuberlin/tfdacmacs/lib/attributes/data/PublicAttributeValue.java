package de.tuberlin.tfdacmacs.lib.attributes.data;

import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.ElementKey;
import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PublicAttributeValue<T> extends ElementKey implements AttributeValueComponent<T> {

    @NotBlank
    private T value;

    @Min(0)
    private long version;

    @NotBlank
    private String signature;

    public PublicAttributeValue(@NotNull @NonNull Element key, @NonNull T value, long version, @NonNull String signature) {
        super(key);
        this.value = value;
        this.version = version;
        this.signature = signature;
    }

    @Override
    public Element getPublicKeyComponent() {
        return getKey();
    }

    public AttributeValue.Public toAttributeValuePublicKey(@NonNull String attributeValueId) {
        return new AttributeValueKey.Public(getKey(), attributeValueId);
    }
}
