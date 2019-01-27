package de.tuberlin.tfdacmacs.attributeauthority.authority;

import de.tuberlin.tfdacmacs.attributeauthority.authority.data.TrustedAuthority;
import de.tuberlin.tfdacmacs.attributeauthority.authority.db.TrustedAuthorityDB;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TrustedAuthorityService {

    private final TrustedAuthorityDB trustedAuthorityDB;

    public boolean existTrustedAuthority(@NonNull String authorityId) {
        return trustedAuthorityDB.exist(authorityId);
    }

    public Stream<TrustedAuthority> findAll() {
        return trustedAuthorityDB.findAll();
    }

    public void createTrustedAuthority(@NonNull TrustedAuthority trustedAuthority) {
        trustedAuthorityDB.insert(trustedAuthority);
    }

    public void deleteTrustedAuthority(@NonNull String trustedAuthorityId) {
        trustedAuthorityDB.remove(trustedAuthorityId);
    }
}
