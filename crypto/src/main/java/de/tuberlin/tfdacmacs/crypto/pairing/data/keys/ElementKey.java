package de.tuberlin.tfdacmacs.crypto.pairing.data.keys;

import de.tuberlin.tfdacmacs.crypto.pairing.data.Versioned;
import it.unisa.dia.gas.jpbc.Element;
import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class ElementKey implements Versioned {

    private @NotNull @NonNull Element key;
    private @Min(0) int version;

    public ElementKey(@NotNull @NonNull Element key) {
        this.key = key;
        this.version = 0;
    }

    public Element getKey() {
        return this.key.getImmutable();
    }

    protected <T extends ElementKey> T incrementVersion() {
        this.version++;
        return (T) this;
    }
}
