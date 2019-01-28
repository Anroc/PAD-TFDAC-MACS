package de.tuberlin.tfdacmacs.client.authority;

import de.tuberlin.tfdacmacs.client.authority.client.AuthorityClient;
import de.tuberlin.tfdacmacs.client.authority.data.TrustedAuthority;
import de.tuberlin.tfdacmacs.client.authority.db.TrustedAuthorityDB;
import de.tuberlin.tfdacmacs.client.authority.events.TrustedAuthorityUpdatedEvent;
import de.tuberlin.tfdacmacs.client.authority.exception.InvalidAuthorityIdentifier;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AuthorityKey;
import de.tuberlin.tfdacmacs.crypto.pairing.policy.AuthorityKeyProvider;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthorityService implements AuthorityKeyProvider {

    private final AuthorityClient authorityClient;
    private final TrustedAuthorityDB trustedAuthorityDB;

    private final ApplicationEventPublisher publisher;

    public Optional<AuthorityKey.Public> findAuthorityPublicKey(@NonNull String authorityId) {
        return authorityClient.findAuthorityKey(authorityId);
    }

    @Override
    public AuthorityKey.Public getAuthorityPublicKey(@NonNull String authorityId) {
        return findAuthorityPublicKey(authorityId)
                .orElseThrow(() -> new InvalidAuthorityIdentifier(authorityId));
    }

    public Optional<TrustedAuthority> findTrustedAuthority(@NonNull String authorityId) {
        return trustedAuthorityDB.find(authorityId);
    }

    public void upsert(@NonNull TrustedAuthority trustedAuthority) {
        trustedAuthorityDB.upsert(trustedAuthority.getId(), trustedAuthority);
        publisher.publishEvent(new TrustedAuthorityUpdatedEvent(trustedAuthority));
    }
}
