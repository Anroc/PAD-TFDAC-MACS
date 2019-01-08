package de.tuberlin.tfdacmacs.client.keypair;

import de.tuberlin.tfdacmacs.client.config.ClientConfig;
import de.tuberlin.tfdacmacs.crypto.rsa.AsymmetricCryptEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.KeyPair;

@Component
@RequiredArgsConstructor
public class KeyPairFactory {

    private final AsymmetricCryptEngine<?> cryptEngine;
    private final ClientConfig clientConfig;

    private KeyPair keyPair;

    public KeyPair getKeyPair() {
        if(keyPair == null) {
            keyPair = cryptEngine.generateKeyPair();
//            saveKeyPair(keyPair);
        }

        return keyPair;
    }

//    private Optional<KeyPair> findFromKeyStore() {
//        GeneralKeyStoreConfig generalKeyStoreConfig = clientConfig.getPrivateKey();
//
//        try {
//            PemObject pemReader = new PemReader(
//                    new FileReader(clientConfig.locateResource(generalKeyStoreConfig.getLocation()))).readPemObject();
//            PrivateKeyInfo info = PrivateKeyInfo.getInstance(ASN1Primitive.fromByteArray(pemReader.getContent()));
//            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pemReader.getContent());
//
//            KeyFactory keyFact =  KeyFactory.getInstance(info.getPrivateKeyAlgorithm().getAlgorithm().getId(), "BC");
//
//            return Optional.of(
//                    new KeyPair(
//                            keyFact.generatePublic(keySpec),
//                            keyFact.generatePrivate(keySpec)
//                    )
//            )
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
