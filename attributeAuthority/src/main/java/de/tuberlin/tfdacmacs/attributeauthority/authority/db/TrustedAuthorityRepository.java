package de.tuberlin.tfdacmacs.attributeauthority.authority.db;

import de.tuberlin.tfdacmacs.attributeauthority.authority.data.TrustedAuthority;
import org.springframework.data.couchbase.core.query.Query;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface TrustedAuthorityRepository extends CouchbaseRepository<TrustedAuthority, String> {

    @Query("#{#n1ql.selectEntity} USE KEYS $1 WHERE `_class` = 'de.tuberlin.tfdacmacs.attributeauthority.authority.data.TrustedAuthority'")
    Optional<TrustedAuthority> findTrustedAuthorityById(String id);

    @Query("#{#n1ql.selectEntity} WHERE `_class` = 'de.tuberlin.tfdacmacs.attributeauthority.authority.data.TrustedAuthority'")
    Stream<TrustedAuthority> findAllTrustedAuthorities();
}
