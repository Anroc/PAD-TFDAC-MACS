package de.tuberlin.tfdacmacs.client.authority;

import de.tuberlin.tfdacmacs.client.authority.client.AuthorityClient;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AuthorityKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthorityService {

    private final AuthorityClient authorityClient;

    public Optional<AuthorityKey.Public> findAuthorityPublicKey(@NonNull String authorityId) {
        return authorityClient.findAuthorityKey(authorityId);
    }
}
