package de.tuberlin.tfdacmacs.centralserver.attribute.db;

import de.tuberlin.tfdacmacs.lib.attributes.data.Attribute;
import de.tuberlin.tfdacmacs.lib.attributes.data.PublicAttribute;
import de.tuberlin.tfdacmacs.lib.db.CouchbaseDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PublicAttributeDB extends CouchbaseDB<PublicAttribute> {

    private final PublicAttributeRepository repository;

    @Autowired
    public PublicAttributeDB(PublicAttributeRepository repository) {
        super(repository, PublicAttribute.class);
        this.repository = repository;
    }

    public List<PublicAttribute> findAll() {
        return repository.findAllByClass().collect(Collectors.toList());
    }
}
