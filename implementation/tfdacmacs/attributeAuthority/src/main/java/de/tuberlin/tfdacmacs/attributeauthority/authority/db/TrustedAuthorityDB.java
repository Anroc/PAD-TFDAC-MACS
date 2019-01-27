package de.tuberlin.tfdacmacs.attributeauthority.authority.db;

import de.tuberlin.tfdacmacs.attributeauthority.authority.data.TrustedAuthority;
import de.tuberlin.tfdacmacs.lib.db.CouchbaseDB;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

@Component
public class TrustedAuthorityDB extends CouchbaseDB<TrustedAuthority> {

    private final TrustedAuthorityRepository repository;

    public TrustedAuthorityDB(TrustedAuthorityRepository repository) {
        super(repository, TrustedAuthority.class);
        this.repository = repository;
    }

    @Override
    public Optional<TrustedAuthority> findEntity(@NonNull String id) {
        return repository.findTrustedAuthorityById(id);
    }

    @Override
    public boolean exist(@NonNull String id) {
        return findEntity(id).isPresent();
    }

    public Stream<TrustedAuthority> findAll() {
        return repository.findAllTrustedAuthorities();
    }
}
