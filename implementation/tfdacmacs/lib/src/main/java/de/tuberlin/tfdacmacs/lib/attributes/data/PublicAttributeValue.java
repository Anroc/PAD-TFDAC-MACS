package de.tuberlin.tfdacmacs.lib.attributes.data;

import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.ElementKey;
import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PublicAttributeValue<T> extends ElementKey implements AttributeValueComponent<T> {

    @NotBlank
    private T value;

    public PublicAttributeValue(@NotNull @NonNull Element key, @NonNull T value) {
        super(key);
        this.value = value;
    }

    @Override
    public Element getPublicKeyComponent() {
        return getKey();
    }
}
