package de.tuberlin.tfdacmacs.centralserver.ciphertext.factory;

import de.tuberlin.tfdacmacs.centralserver.ciphertext.data.CipherTextEntity;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.VersionedID;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Data
public class CipherTextTestFactory {

    private GlobalPublicParameter globalPublicParameter;

    public CipherTextEntity createRandom() {
        return create("aa.tu-berlin.de.role:student");
    }

    public CipherTextEntity create(String... policy) {
        return new CipherTextEntity(
                UUID.randomUUID().toString(),
                globalPublicParameter.gt().newRandomElement(),
                globalPublicParameter.g1().newRandomElement(),
                globalPublicParameter.g1().newRandomElement(),
                Arrays.stream(policy).map(p -> new VersionedID(p, 0L)).collect(Collectors.toSet()),
                new VersionedID(UUID.randomUUID().toString(), 0L),
                "encryptedMessage");
    }
}
