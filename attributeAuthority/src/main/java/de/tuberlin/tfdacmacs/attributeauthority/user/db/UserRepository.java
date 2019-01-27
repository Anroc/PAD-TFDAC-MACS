package de.tuberlin.tfdacmacs.attributeauthority.user.db;

import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
import org.springframework.data.couchbase.core.query.Query;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CouchbaseRepository<User, String> {

    @Query("#{#n1ql.selectEntity} USE KEYS $1 WHERE `_class` = 'de.tuberlin.tfdacmacs.attributeauthority.user.data.User'")
    Optional<User> findUserById(String id);
}
