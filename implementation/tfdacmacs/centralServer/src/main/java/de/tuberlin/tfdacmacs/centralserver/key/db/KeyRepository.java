package de.tuberlin.tfdacmacs.centralserver.key.db;

import de.tuberlin.tfdacmacs.centralserver.key.data.RsaKeyPair;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeyRepository extends CouchbaseRepository<RsaKeyPair, String> {
}
