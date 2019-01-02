package de.tuberlin.tfdacmacs.attributeauthority.attribute.db;

import de.tuberlin.tfdacmacs.lib.attributes.data.Attribute;
import de.tuberlin.tfdacmacs.lib.db.CouchbaseDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class AttributeDB extends CouchbaseDB<Attribute> {

    private final AttributeRepository repository;

    @Autowired
    public AttributeDB(
            AttributeRepository repository) {
        super(repository, Attribute.class);
        this.repository = repository;
    }

    public Collection<Attribute> findAll() {
        return repository.findAllByClass().collect(Collectors.toList());
    }
}
