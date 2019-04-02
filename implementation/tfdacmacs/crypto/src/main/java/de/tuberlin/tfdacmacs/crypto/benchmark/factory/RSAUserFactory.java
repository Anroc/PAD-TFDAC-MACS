package de.tuberlin.tfdacmacs.crypto.benchmark.factory;

import de.tuberlin.tfdacmacs.crypto.benchmark.rsa.RSAUser;
import de.tuberlin.tfdacmacs.crypto.rsa.AsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;

import java.security.KeyPair;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RSAUserFactory extends UserFactory<RSAUser> {

    private final AsymmetricCryptEngine asymmetricCryptEngine = new StringAsymmetricCryptEngine();

    @Override
    public List<RSAUser> create(int num) {
        return IntStream.range(0, num).mapToObj((i) -> {
            KeyPair keyPair = newKeyPair();
            return new RSAUser(keyPair.getPrivate(), keyPair.getPublic());
        }).collect(Collectors.toList());
    }

    private KeyPair newKeyPair() {
        return asymmetricCryptEngine.generateKeyPair();
    }
}
