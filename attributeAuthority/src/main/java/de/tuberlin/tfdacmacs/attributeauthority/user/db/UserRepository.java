package de.tuberlin.tfdacmacs.attributeauthority.user.db;

import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
import org.springframework.data.couchbase.core.query.Query;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface UserRepository extends CouchbaseRepository<User, String> {

    @Query("#{#n1ql.selectEntity} USE KEYS $1 WHERE `_class` = 'de.tuberlin.tfdacmacs.attributeauthority.user.data.User'")
    Optional<User> findUserById(String id);

    @Query("#{#n1ql.selectEntity} WHERE `_class` = 'de.tuberlin.tfdacmacs.attributeauthority.user.data.User'"
            + " AND ANY p IN attributes SATISFIES p.attributeId = $1 AND p.`value` = $2 END ")
    Stream<User> findUsersByAttributeIdAndValue(String attributeId, Object value);
}
