package de.tuberlin.tfdacmacs.centralserver.authority;

import de.tuberlin.tfdacmacs.centralserver.authority.data.AttributeAuthority;
import de.tuberlin.tfdacmacs.centralserver.authority.db.AttributeAuthorityDB;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttributeAuthorityService {

    private final AttributeAuthorityDB attributeAuthorityDB;

    public boolean exist(@NonNull String id) {
        return attributeAuthorityDB.exist(id);
    }

    public void insert(@NonNull AttributeAuthority attributeAuthority) {
        attributeAuthorityDB.insert(attributeAuthority);
    }
}
