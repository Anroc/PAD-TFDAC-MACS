package de.tuberlin.tfdacmacs.centralserver.certificate.db;

import de.tuberlin.tfdacmacs.centralserver.certificate.data.Certificate;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificateRepository extends CouchbaseRepository<Certificate, String> {
}
