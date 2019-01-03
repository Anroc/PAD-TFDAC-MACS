package de.tuberlin.tfdacmacs.centralserver.authority.db;

import de.tuberlin.tfdacmacs.centralserver.authority.data.AttributeAuthority;
import de.tuberlin.tfdacmacs.lib.db.CouchbaseDB;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AttributeAuthorityDB extends CouchbaseDB {

    private final AttributeAuthorityRepository repository;

    public AttributeAuthorityDB(AttributeAuthorityRepository repository) {
        super(repository, AttributeAuthority.class);
        this.repository = repository;
    }

    public Optional<AttributeAuthority> findEntity(@NonNull String id) {
        return repository.findByIdAndClass(id);
    }
}
