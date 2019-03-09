package de.tuberlin.tfdacmacs.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;

public class TwoFactorUpdateKey extends UserUpdateKey {
    public TwoFactorUpdateKey(@NonNull String userId, @NonNull Element updateKey, long targetVersion) {
        super(userId, updateKey, targetVersion);
    }
}
