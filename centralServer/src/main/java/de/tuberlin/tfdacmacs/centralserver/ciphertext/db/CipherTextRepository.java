package de.tuberlin.tfdacmacs.centralserver.ciphertext.db;

import com.couchbase.client.java.document.json.JsonArray;
import de.tuberlin.tfdacmacs.centralserver.ciphertext.data.CipherTextEntity;
import org.springframework.data.couchbase.core.query.Query;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface CipherTextRepository extends CouchbaseRepository<CipherTextEntity, String> {

    @Query("#{#n1ql.selectEntity} WHERE `_class` = 'de.tuberlin.tfdacmacs.centralserver.ciphertext.data.CipherTextEntity'")
    Stream<CipherTextEntity> findAllCipherTexts();

    @Query("#{#n1ql.selectEntity} "
            + "WHERE `_class` = 'de.tuberlin.tfdacmacs.centralserver.ciphertext.data.CipherTextEntity' "
            + "AND EVERY p IN accessPolicy SATISFIES p.id IN $1 END")
    Stream<CipherTextEntity> findAllByPolicyContainingAll(JsonArray from);

    @Query("#{#n1ql.selectEntity} "
            + "WHERE `_class` = 'de.tuberlin.tfdacmacs.centralserver.ciphertext.data.CipherTextEntity' "
            + "AND ANY p IN accessPolicy SATISFIES p.id IN $1 END")
    Stream<CipherTextEntity> findAllByPolicyContainingAny(JsonArray from);

    @Query("#{#n1ql.selectEntity} "
            + "WHERE `_class` = 'de.tuberlin.tfdacmacs.centralserver.ciphertext.data.CipherTextEntity' "
            + "AND ownerId.id = $1")
    Stream<CipherTextEntity> findAllByOwnerId(String ownerId);
}
