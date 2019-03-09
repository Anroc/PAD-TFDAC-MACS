package de.tuberlin.tfdacmacs.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class UserUpdateKey extends UpdateKey {

    private final String userId;

    public UserUpdateKey(@NonNull String userId, @NonNull Element updateKey, int targetVersion) {
        super(updateKey, targetVersion);
         this.userId = userId;
    }
}
