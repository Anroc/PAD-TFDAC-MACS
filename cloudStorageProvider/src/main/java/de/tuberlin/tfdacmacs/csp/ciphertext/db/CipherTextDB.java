package de.tuberlin.tfdacmacs.csp.ciphertext.db;

import com.couchbase.client.java.document.json.JsonArray;
import de.tuberlin.tfdacmacs.csp.ciphertext.data.CipherTextEntity;
import de.tuberlin.tfdacmacs.lib.db.CouchbaseDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CipherTextDB extends CouchbaseDB<CipherTextEntity> {

    private CipherTextRepository reposiroty;

    @Autowired
    public CipherTextDB(CipherTextRepository repository) {
        super(repository, CipherTextEntity.class);
        this.reposiroty = repository;
    }

    public List<CipherTextEntity> findAll() {
        return reposiroty.findAllCipherTexts().collect(Collectors.toList());
    }

    public List<CipherTextEntity> findAllByPolicyContaining(List<String> attributeIds) {
        return reposiroty.findAllByPolicyContaining(JsonArray.from(attributeIds)).collect(Collectors.toList());
    }
}
