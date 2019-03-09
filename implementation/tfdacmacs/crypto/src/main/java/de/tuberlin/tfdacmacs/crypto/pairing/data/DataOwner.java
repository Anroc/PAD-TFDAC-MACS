package de.tuberlin.tfdacmacs.crypto.pairing.data;

import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorKey;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class DataOwner {

    private final VersionedID id;
    private final TwoFactorKey.Private twoFactorPrivateKey;

    public DataOwner(String id,
            TwoFactorKey.Private twoFactorPrivateKey) {
        this.id = new VersionedID(id, twoFactorPrivateKey.getVersion());
        this.twoFactorPrivateKey = twoFactorPrivateKey;
    }
}
