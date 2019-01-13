package de.tuberlin.tfdacmacs.csp.ciphertext.db;

import de.tuberlin.tfdacmacs.csp.ciphertext.data.CipherTextEntity;
import de.tuberlin.tfdacmacs.lib.db.CouchbaseDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CipherTextDB extends CouchbaseDB<CipherTextEntity> {

    @Autowired
    public CipherTextDB(CipherTextRepository repository) {
        super(repository, CipherTextEntity.class);
    }
}
