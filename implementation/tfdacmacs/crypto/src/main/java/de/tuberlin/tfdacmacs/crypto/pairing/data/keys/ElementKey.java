package de.tuberlin.tfdacmacs.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.*;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class ElementKey {

    @NotNull
    private @NonNull Element key;

    public Element getKey() {
        return this.key.getImmutable();
    }
}
