package de.tuberlin.tfdacmacs.client.attribute.db;

import de.tuberlin.tfdacmacs.client.attribute.data.Attribute;
import de.tuberlin.tfdacmacs.client.db.JsonDB;
import org.springframework.stereotype.Component;

@Component
public class AttributeDB extends JsonDB<Attribute> {

    public AttributeDB() {
        super(Attribute.class);
    }
}
