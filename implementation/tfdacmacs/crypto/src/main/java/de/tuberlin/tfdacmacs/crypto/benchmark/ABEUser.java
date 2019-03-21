package de.tuberlin.tfdacmacs.crypto.benchmark;

import de.tuberlin.tfdacmacs.crypto.pairing.data.DataOwner;
import de.tuberlin.tfdacmacs.crypto.pairing.data.UserAttributeSecretComponent;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorKey;
import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class ABEUser extends User {

    private Set<UserAttributeSecretComponent> attributes;
    private TwoFactorKey twoFactorKey;

    private Map<String, TwoFactorKey.Public> tfPublics;

    private boolean useTowFactorKey;

    public DataOwner asDataOwner() {
        if(useTowFactorKey) {
            return new DataOwner(getId(), twoFactorKey.getPrivateKey());
        } else {
            return null;
        }
    }

    public TwoFactorKey.Public getTwoFactorPublicKey(String ownerId) {
        if(ownerId == null || ! useTowFactorKey) {
            return null;
        }

        return tfPublics.get(ownerId);
    }

}