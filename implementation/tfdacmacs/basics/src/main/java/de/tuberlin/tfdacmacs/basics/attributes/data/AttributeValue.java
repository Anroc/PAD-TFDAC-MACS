package de.tuberlin.tfdacmacs.basics.attributes.data;

import it.unisa.dia.gas.jpbc.Element;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeValue<T> {

    @NotNull
    private Element privateKey;
    @NotNull
    private Element publicKey;

    @NotNull
    private T value;

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
