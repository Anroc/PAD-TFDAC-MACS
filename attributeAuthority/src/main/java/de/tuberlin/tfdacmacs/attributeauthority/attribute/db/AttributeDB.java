package de.tuberlin.tfdacmacs.attributeauthority.attribute.db;

import com.couchbase.client.java.Bucket;
import de.tuberlin.tfdacmacs.basics.attributes.data.Attribute;
import de.tuberlin.tfdacmacs.basics.db.CouchbaseDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.stereotype.Component;

@Component
public class AttributeDB extends CouchbaseDB<Attribute> {
    @Autowired
    public AttributeDB(Bucket bucket,
            AttributeRepository repository,
            CouchbaseTemplate template) {
        super(bucket, repository, template, Attribute.class);
    }
}
