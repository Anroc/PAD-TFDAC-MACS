package de.tuberlin.tfdacmacs.crypto.benchmark.pairing;

import de.tuberlin.tfdacmacs.crypto.benchmark.User;
import de.tuberlin.tfdacmacs.crypto.pairing.data.DataOwner;
import de.tuberlin.tfdacmacs.crypto.pairing.data.UserAttributeSecretComponent;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorKey;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class ABEUser extends User {

    private Set<UserAttributeSecretComponent> attributes;
    private Map<String, TwoFactorKey.Secret> tfPublics = new HashMap<>();

    private TwoFactorKey twoFactorKey;
    private boolean useTowFactorKey = false;

    public ABEUser(String id, Set<UserAttributeSecretComponent> attributes) {
        super(id);
        this.attributes = attributes;
    }

    public DataOwner asDataOwner() {
        if(useTowFactorKey) {
            return new DataOwner(getId(), twoFactorKey.getPrivateKey());
        } else {
            return null;
        }
    }

    public TwoFactorKey.Secret getTwoFactorPublicKey(String ownerId) {
        if(ownerId == null || ! useTowFactorKey) {
            return null;
        }

        return tfPublics.get(ownerId);
    }

}
