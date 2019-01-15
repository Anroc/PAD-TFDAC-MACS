package de.tuberlin.tfdacmacs.centralserver.authority;

import de.tuberlin.tfdacmacs.centralserver.authority.data.AttributeAuthority;
import de.tuberlin.tfdacmacs.centralserver.authority.db.AttributeAuthorityDB;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AuthorityKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttributeAuthorityService {

    private final AttributeAuthorityDB attributeAuthorityDB;

    public Optional<AttributeAuthority> findEntity(@NonNull String id) {
        return attributeAuthorityDB.findEntity(id);
    }

    public boolean exist(@NonNull String id) {
        return findEntity(id).isPresent();
    }

    public void insert(@NonNull AttributeAuthority attributeAuthority) {
        attributeAuthorityDB.insert(attributeAuthority);
    }

    public AttributeAuthority updatePublicKey(@NonNull AttributeAuthority attributeAuthority, @NonNull AuthorityKey.Public publicKey) {
        attributeAuthority.setPublicKey(publicKey);
        attributeAuthorityDB.update(attributeAuthority);
        return attributeAuthority;
    }
}
