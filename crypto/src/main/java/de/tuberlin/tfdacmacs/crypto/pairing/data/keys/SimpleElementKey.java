package de.tuberlin.tfdacmacs.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class SimpleElementKey extends ElementKey {

    public SimpleElementKey(@NotNull @NonNull Element key, long version) {
        super(key, version);
    }
}
