package de.tuberlin.tfdacmacs.centralserver.twofactorkey;

import de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.EncryptedTwoFactorKey;
import de.tuberlin.tfdacmacs.centralserver.twofactorkey.db.TwoFactorKeyDB;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TwoFactorKeyService {

    private final TwoFactorKeyDB twoFactorKeyDB;

    public void insert(@NonNull EncryptedTwoFactorKey encryptedTwoFactorKey) {
        twoFactorKeyDB.insert(encryptedTwoFactorKey);
    }

    public Optional<EncryptedTwoFactorKey> findTwoFactorKey(@NonNull String id) {
        return twoFactorKeyDB.findEntity(id);
    }

    public Stream<EncryptedTwoFactorKey> findByUserIdOrOwnerId(@NonNull String userId) {
        return twoFactorKeyDB.findByUserIdOrOwnerId(userId);
    }

    public void update(@NonNull EncryptedTwoFactorKey encryptedTwoFactorKey) {
        twoFactorKeyDB.update(encryptedTwoFactorKey);
    }

    public void remove(@NonNull String id) {
        twoFactorKeyDB.remove(id);
    }
}
