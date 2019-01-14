package de.tuberlin.tfdacmacs.client.attribute;

import de.tuberlin.tfdacmacs.client.attribute.data.Attribute;
import de.tuberlin.tfdacmacs.client.attribute.data.dto.DeviceResponse;
import de.tuberlin.tfdacmacs.client.attribute.data.dto.EncryptedAttributeValueKeyDTO;
import de.tuberlin.tfdacmacs.client.gpp.GPPService;
import de.tuberlin.tfdacmacs.client.keypair.KeyPairService;
import de.tuberlin.tfdacmacs.client.rest.CaClient;
import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import de.tuberlin.tfdacmacs.crypto.rsa.AsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.SymmetricCryptEngine;
import it.unisa.dia.gas.jpbc.Element;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AttributeClient {

    private final CaClient caClient;
    private final KeyPairService keyPairService;
    private final AsymmetricCryptEngine<?> asymmetricCryptEngine;
    private final SymmetricCryptEngine<?> symmetricCryptEngine;

    private final GPPService gppService;

    public Set<Attribute> getAttributes(String email, String certificateId) {
        DeviceResponse deviceResponse = caClient.getAttributes(email, certificateId);
        String encryptedKey = deviceResponse.getEncryptedKey();
        Set<EncryptedAttributeValueKeyDTO> encryptedAttributeValueKeys = deviceResponse
                .getEncryptedAttributeValueKeys();

        return decrypt(keyPairService.getKeyPair(email).getPrivateKey(), encryptedKey, encryptedAttributeValueKeys);
    }

    private Set<Attribute> decrypt(PrivateKey privateKey, String encryptedKey, Set<EncryptedAttributeValueKeyDTO> encryptedAttributeValueKeys) {
        try {
            Key key = symmetricCryptEngine.createKeyFromBytes(
                    asymmetricCryptEngine.decryptRaw(encryptedKey, privateKey));

            return encryptedAttributeValueKeys.stream()
                    .map(encryptedAttributeValueKeyDTO -> decrypt(key, encryptedAttributeValueKeyDTO))
                    .collect(Collectors.toSet());
        } catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }

    private Attribute decrypt(Key symmetricKey, EncryptedAttributeValueKeyDTO encryptedAttributeValueKeyDTO) {
        try {
            byte[] rawElement = symmetricCryptEngine.decryptRaw(encryptedAttributeValueKeyDTO.getEncryptedKey(), symmetricKey);
            Element element = ElementConverter.convert(rawElement, gppService.getGPP().getPairing().getG1());

            return new Attribute(encryptedAttributeValueKeyDTO.getAttributeValueId(), element);
        } catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }
}
