package de.tuberlin.tfdacmacs.centralserver.key.db;

import de.tuberlin.tfdacmacs.basics.db.CouchbaseDB;
import de.tuberlin.tfdacmacs.centralserver.key.data.RsaKeyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class KeyDB extends CouchbaseDB<RsaKeyPair> {

    @Autowired
    public KeyDB(KeyRepository repository) {
        super(repository, RsaKeyPair.class);
    }

    public Optional<RsaKeyPair> findEntity() {
        return super.findEntity(RsaKeyPair.ID);
    }

    public boolean existRsaKeyPair() {
        return super.exist(RsaKeyPair.ID);
    }
}
