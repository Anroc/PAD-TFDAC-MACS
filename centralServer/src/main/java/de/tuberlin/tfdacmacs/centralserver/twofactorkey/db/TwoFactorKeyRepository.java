package de.tuberlin.tfdacmacs.centralserver.twofactorkey.db;

import de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.EncryptedTwoFactorKey;
import org.springframework.data.couchbase.core.query.Query;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface TwoFactorKeyRepository extends CouchbaseRepository<EncryptedTwoFactorKey, String> {

    @Query("#{#n1ql.delete} USE KEYS $1 "
            + "WHERE `_class` = 'de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.EncryptedTwoFactorKey'")
    Optional<EncryptedTwoFactorKey> deleteByIdAndClass(String id);

    @Query("#{#n1ql.selectEntity} "
            + "WHERE (userId = $1 OR dataOwnerId = $1) AND `_class` = 'de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.EncryptedTwoFactorKey'")
    Stream<EncryptedTwoFactorKey> findByUserIdOrOwnerId(String userId);

}
