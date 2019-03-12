package de.tuberlin.tfdacmacs.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public abstract class UserUpdateKey extends UpdateKey {

    private String userId;

    public UserUpdateKey(@NonNull String userId, @NonNull Element updateKey, long targetVersion) {
        super(updateKey, targetVersion);
         this.userId = userId;
    }
}
