package de.tuberlin.tfdacmacs.basics.crypto.pairing.data;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.TwoFactorKey;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class DataOwner {

    private final String id;
    private final TwoFactorKey.Private twoFactorPrivateKey;
}
