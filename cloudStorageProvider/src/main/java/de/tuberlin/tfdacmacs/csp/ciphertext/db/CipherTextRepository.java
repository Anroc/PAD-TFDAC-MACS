package de.tuberlin.tfdacmacs.csp.ciphertext.db;

import de.tuberlin.tfdacmacs.csp.ciphertext.data.CipherTextEntity;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CipherTextRepository extends CouchbaseRepository<CipherTextEntity, String> {
}
