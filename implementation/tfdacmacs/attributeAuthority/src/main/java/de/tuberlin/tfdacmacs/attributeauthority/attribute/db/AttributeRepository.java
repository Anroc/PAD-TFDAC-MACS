package de.tuberlin.tfdacmacs.attributeauthority.attribute.db;

import de.tuberlin.tfdacmacs.lib.attributes.data.Attribute;
import org.springframework.data.couchbase.core.query.Query;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface AttributeRepository extends CouchbaseRepository<Attribute, String> {

    @Query("#{#n1ql.selectEntity} WHERE `_class` = 'de.tuberlin.tfdacmacs.basics.attributes.data.Attribute'")
    Stream<Attribute> findAllByClass();
}
