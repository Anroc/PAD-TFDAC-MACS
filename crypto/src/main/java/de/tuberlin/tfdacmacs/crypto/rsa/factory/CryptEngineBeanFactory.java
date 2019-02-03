package de.tuberlin.tfdacmacs.crypto.rsa.factory;

import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class CryptEngineBeanFactory {

    @Bean
    public StringAsymmetricCryptEngine stringAsymmetricCryptEngine() {
        StringAsymmetricCryptEngine cryptEngine = new StringAsymmetricCryptEngine();
        cryptEngine.setAsymmetricCipherKeyPair(cryptEngine.generateKeyPair());
        return cryptEngine;
    }
}
