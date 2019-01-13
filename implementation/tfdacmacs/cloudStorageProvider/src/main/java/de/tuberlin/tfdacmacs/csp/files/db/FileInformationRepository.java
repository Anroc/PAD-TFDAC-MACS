package de.tuberlin.tfdacmacs.csp.files.db;

import de.tuberlin.tfdacmacs.csp.files.data.FileInformation;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileInformationRepository extends CouchbaseRepository<FileInformation, String> {
}
