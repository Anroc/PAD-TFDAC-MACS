package de.tuberlin.tfdacmacs.centralserver.attribute.db;

import de.tuberlin.tfdacmacs.lib.attributes.data.PublicAttribute;
import org.springframework.data.couchbase.core.query.Query;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface PublicAttributeRepository extends CouchbaseRepository<PublicAttribute, String> {

    @Query("#{#n1ql.selectEntity} WHERE `_class` = 'de.tuberlin.tfdacmacs.lib.attributes.data.PublicAttribute'")
    Stream<PublicAttribute> findAllByClass();
}
