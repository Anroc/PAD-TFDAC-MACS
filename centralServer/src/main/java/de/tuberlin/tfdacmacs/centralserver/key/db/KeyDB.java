package de.tuberlin.tfdacmacs.centralserver.key.db;

import com.couchbase.client.java.Bucket;
import de.tuberlin.tfdacmacs.basics.db.CouchbaseDB;
import de.tuberlin.tfdacmacs.centralserver.key.data.RsaKeyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class KeyDB extends CouchbaseDB<RsaKeyPair> {

    @Autowired
    public KeyDB(Bucket bucket,
            KeyRepository repository,
            CouchbaseTemplate template) {
        super(bucket, repository, template, RsaKeyPair.class);
    }

    public Optional<RsaKeyPair> findEntity() {
        return super.findEntity(RsaKeyPair.ID);
    }

    public boolean existRsaKeyPair() {
        return super.exist(RsaKeyPair.ID);
    }
}
