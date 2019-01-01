package de.tuberlin.tfdacmacs.centralserver.authority.db;

import de.tuberlin.tfdacmacs.centralserver.authority.data.AttributeAuthority;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttributeAuthorityRepository extends CouchbaseRepository<AttributeAuthority, String> {
}
