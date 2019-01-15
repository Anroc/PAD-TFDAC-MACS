package de.tuberlin.tfdacmacs.client.authority;

import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AuthorityKey;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthorityService {

    public Optional<AuthorityKey.Public> findAuthorityPublicKey(@NonNull String authorityId) {
        // TODO: implement
        return null;
    }
}
