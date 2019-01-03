package de.tuberlin.tfdacmacs.centralserver.authority.db;

import de.tuberlin.tfdacmacs.centralserver.authority.data.AttributeAuthority;
import org.springframework.data.couchbase.core.query.Query;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttributeAuthorityRepository extends CouchbaseRepository<AttributeAuthority, String> {

    @Query("#{#n1ql.selectEntity} USE KEYS $1 WHERE `_class` = 'de.tuberlin.tfdacmacs.centralserver.authority.data.AttributeAuthority'")
    Optional<AttributeAuthority> findByIdAndClass(String id);
}
