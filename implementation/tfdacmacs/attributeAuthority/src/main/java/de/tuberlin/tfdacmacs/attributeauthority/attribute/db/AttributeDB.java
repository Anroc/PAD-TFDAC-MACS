package de.tuberlin.tfdacmacs.attributeauthority.attribute.db;

import com.couchbase.client.java.Bucket;
import de.tuberlin.tfdacmacs.basics.attributes.data.Attribute;
import de.tuberlin.tfdacmacs.basics.db.CouchbaseDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class AttributeDB extends CouchbaseDB<Attribute> {

    private final AttributeRepository repository;

    @Autowired
    public AttributeDB(Bucket bucket,
            AttributeRepository repository,
            CouchbaseTemplate template) {
        super(bucket, repository, template, Attribute.class);
        this.repository = repository;
    }

    public Collection<Attribute> findAll() {
        return repository.findAllByClass().collect(Collectors.toList());
    }
}
