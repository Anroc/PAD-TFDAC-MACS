package de.tuberlin.tfdacmacs.centralserver.ciphertext.factory;

import com.google.common.collect.Sets;
import de.tuberlin.tfdacmacs.centralserver.ciphertext.data.CipherTextEntity;
import de.tuberlin.tfdacmacs.crypto.GPPTestFactory;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CipherTextTestFactory {

    private final GPPTestFactory gppTestFactory;

    private GlobalPublicParameter globalPublicParameter;

    @PostConstruct
    public void setup() {
        this.globalPublicParameter = gppTestFactory.create();
    }

    public CipherTextEntity createRandom() {
        return create("aa.tu-berlin.de.role:student");
    }

    public CipherTextEntity create(String... policy) {
        return new CipherTextEntity(
                UUID.randomUUID().toString(),
                globalPublicParameter.getPairing().getG1().newRandomElement(),
                globalPublicParameter.getPairing().getG1().newRandomElement(),
                globalPublicParameter.getPairing().getG1().newRandomElement(),
                Arrays.stream(policy).collect(Collectors.toSet()),
                UUID.randomUUID().toString(),
                "encryptedMessage");
    }
}
