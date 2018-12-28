package de.tuberlin.tfdacmacs.attributeauthority.attribute.db;

import de.tuberlin.tfdacmacs.basics.attributes.data.Attribute;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttributeRepository extends CouchbaseRepository<Attribute, String> {
}
