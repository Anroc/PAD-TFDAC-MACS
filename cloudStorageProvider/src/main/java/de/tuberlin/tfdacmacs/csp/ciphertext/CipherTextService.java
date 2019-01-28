package de.tuberlin.tfdacmacs.csp.ciphertext;

import de.tuberlin.tfdacmacs.csp.ciphertext.data.CipherTextEntity;
import de.tuberlin.tfdacmacs.csp.ciphertext.db.CipherTextDB;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CipherTextService {

    private final CipherTextDB cipherTextDB;

    public void insert(@NonNull CipherTextEntity cipherTextEntity) {
        cipherTextDB.insert(cipherTextEntity);
    }

    public Optional<CipherTextEntity> findCipherText(String id) {
        return cipherTextDB.findEntity(id);
    }

    public List<CipherTextEntity> findAll() {
        return cipherTextDB.findAll();
    }

    public List<CipherTextEntity> findAllByPolicyContaining(List<String> attributeIds) {
        return cipherTextDB.findAllByPolicyContaining(attributeIds);
    }
}
