package de.tuberlin.tfdacmacs.crypto.pairing.data.keys;

import de.tuberlin.tfdacmacs.crypto.pairing.exceptions.VersionMismatchException;
import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class UpdateKey extends ElementKey {

    public UpdateKey(@NotNull @NonNull Element key, long targetVersion) {
        super(key, targetVersion);
    }

    public Element getUpdateKey() {
        return getKey();
    }

    public void checkApplicablilty(@NonNull ElementKey target) {
        if(target.getVersion() != getVersion()) {
            throw new VersionMismatchException(target, this);
        }
    }
}
