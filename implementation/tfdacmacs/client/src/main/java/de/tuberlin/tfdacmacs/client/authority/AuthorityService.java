package de.tuberlin.tfdacmacs.client.authority;

import de.tuberlin.tfdacmacs.client.authority.client.AuthorityClient;
import de.tuberlin.tfdacmacs.client.authority.exception.InvalidAuthorityIdentifier;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AuthorityKey;
import de.tuberlin.tfdacmacs.crypto.pairing.policy.AuthorityKeyProvider;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthorityService implements AuthorityKeyProvider {

    private final AuthorityClient authorityClient;

    public Optional<AuthorityKey.Public> findAuthorityPublicKey(@NonNull String authorityId) {
        return authorityClient.findAuthorityKey(authorityId);
    }

    @Override
    public AuthorityKey.Public getAuthorityPublicKey(@NonNull String authorityId) {
        return findAuthorityPublicKey(authorityId)
                .orElseThrow(() -> new InvalidAuthorityIdentifier(authorityId));
    }
}
