package de.tuberlin.tfdacmacs.ciphertext.factory;

import com.google.common.collect.Sets;
import de.tuberlin.tfdacmacs.crypto.GPPTestFactory;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.csp.ciphertext.data.CipherTextEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;

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
        return new CipherTextEntity(
                UUID.randomUUID().toString(),
                globalPublicParameter.getPairing().getG1().newRandomElement(),
                globalPublicParameter.getPairing().getG1().newRandomElement(),
                globalPublicParameter.getPairing().getG1().newRandomElement(),
                Sets.newHashSet("aa.tu-berlin.de.role:student"),
                UUID.randomUUID().toString(),
                "encryptedMessage");
    }
}
