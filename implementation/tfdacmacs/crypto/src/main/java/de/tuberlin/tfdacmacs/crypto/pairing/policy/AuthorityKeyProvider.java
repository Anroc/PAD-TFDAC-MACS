package de.tuberlin.tfdacmacs.crypto.pairing.policy;

import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AuthorityKey;
import lombok.NonNull;

public interface AuthorityKeyProvider {

    AuthorityKey.Public getAuthorityPublicKey(@NonNull String authorityId);

}
