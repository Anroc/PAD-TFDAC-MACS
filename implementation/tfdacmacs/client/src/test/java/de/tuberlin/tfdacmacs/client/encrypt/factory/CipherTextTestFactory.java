package de.tuberlin.tfdacmacs.client.encrypt.factory;

import de.tuberlin.tfdacmacs.crypto.pairing.data.CipherText;
import de.tuberlin.tfdacmacs.crypto.pairing.data.VersionedID;
import it.unisa.dia.gas.jpbc.Field;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CipherTextTestFactory {

    private Field g1;
    private Field gt;

    public void postConstruct(Field g1, Field gt) {
        this.g1 = g1;
        this.gt = gt;
    }

    public CipherText create() {
        return create(UUID.randomUUID().toString(), null);
    }

    public CipherText create(String id, String ownerId, String... policy) {
        return new CipherText(
                id,
                gt.newRandomElement(),
                g1.newRandomElement(),
                g1.newRandomElement(),
                Arrays.stream(policy).map(p -> new VersionedID(p, 0L)).collect(Collectors.toSet()),
                new VersionedID(ownerId, 0L),
                UUID.randomUUID().toString()
        );
    }
}
