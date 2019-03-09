package de.tuberlin.tfdacmacs.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class UpdateKey extends ElementKey {

    private int targetVersion;

    public UpdateKey(@NotNull @NonNull Element key, int targetVersion) {
        super(key);
        this.targetVersion = targetVersion;
    }

    public Element getUpdateKey() {
        return getKey();
    }
}
