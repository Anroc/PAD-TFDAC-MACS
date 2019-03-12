package de.tuberlin.tfdacmacs.centralserver.ciphertext.db;

import com.couchbase.client.java.document.json.JsonArray;
import de.tuberlin.tfdacmacs.centralserver.ciphertext.data.CipherTextEntity;
import de.tuberlin.tfdacmacs.lib.db.CouchbaseDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CipherTextDB extends CouchbaseDB<CipherTextEntity> {

    private CipherTextRepository repository;

    @Autowired
    public CipherTextDB(CipherTextRepository repository) {
        super(repository, CipherTextEntity.class);
        this.repository = repository;
    }

    public List<CipherTextEntity> findAll() {
        return repository.findAllCipherTexts().collect(Collectors.toList());
    }

    public List<CipherTextEntity> findAllByPolicyContaining(List<String> attributeIds, boolean complete) {
        if(complete) {
            return repository.findAllByPolicyContainingAll(JsonArray.from(attributeIds)).collect(Collectors.toList());
        }
        return repository.findAllByPolicyContainingAny(JsonArray.from(attributeIds)).collect(Collectors.toList());
    }

    public List<CipherTextEntity> findAllByOwnerId(String ownerId) {
        return repository.findAllByOwnerId(ownerId).collect(Collectors.toList());
    }

    public Optional<CipherTextEntity> findById(String id){
        return repository.findById(id);
    }
}
