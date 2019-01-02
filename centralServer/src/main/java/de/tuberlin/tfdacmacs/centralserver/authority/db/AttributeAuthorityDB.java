package de.tuberlin.tfdacmacs.centralserver.authority.db;

import de.tuberlin.tfdacmacs.lib.db.CouchbaseDB;
import de.tuberlin.tfdacmacs.centralserver.authority.data.AttributeAuthority;
import org.springframework.stereotype.Component;

@Component
public class AttributeAuthorityDB extends CouchbaseDB {
    public AttributeAuthorityDB(AttributeAuthorityRepository repository) {
        super(repository, AttributeAuthority.class);
    }
}
