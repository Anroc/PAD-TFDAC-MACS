package de.tuberlin.tfdacmacs.crypto.pairing.data.keys;

import de.tuberlin.tfdacmacs.crypto.pairing.data.Versioned;
import it.unisa.dia.gas.jpbc.Element;
import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public abstract class ElementKey implements Versioned {

    private @NotNull @NonNull Element key;
    private @Min(0) long version;

    public ElementKey(@NotNull @NonNull Element key, long version) {
        this.key = key.getImmutable();
        this.version = version;
    }

    protected <T extends ElementKey> T incrementVersion() {
        setVersion(getVersion() + 1L);
        return (T) this;
    }

    protected <T extends ElementKey> T update(@NonNull Element newKey) {
        setKey(newKey);
        incrementVersion();
        return (T) this;
    }
}
