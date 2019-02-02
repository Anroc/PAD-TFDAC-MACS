package de.tuberlin.tfdacmacs.centralserver.twofactorkey.db;

import de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.EncryptedTwoFactorKey;
import de.tuberlin.tfdacmacs.lib.db.CouchbaseDB;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

@Component
public class TwoFactorKeyDB extends CouchbaseDB<EncryptedTwoFactorKey> {

    private final TwoFactorKeyRepository repository;

    @Autowired
    public TwoFactorKeyDB(TwoFactorKeyRepository repository) {
        super(repository, EncryptedTwoFactorKey.class);
        this.repository = repository;
    }

    @Override
    public Optional<EncryptedTwoFactorKey> remove(@NonNull String id) {
        return repository.deleteByIdAndClass(id);
    }

    public Stream<EncryptedTwoFactorKey> findByUserIdOrOwnerId(@NonNull String userId) {
        return repository.findByUserIdOrOwnerId(userId);
    }
}
