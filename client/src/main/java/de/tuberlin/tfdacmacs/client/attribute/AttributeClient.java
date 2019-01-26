package de.tuberlin.tfdacmacs.client.attribute;

import de.tuberlin.tfdacmacs.client.attribute.data.Attribute;
import de.tuberlin.tfdacmacs.client.attribute.data.dto.DeviceResponse;
import de.tuberlin.tfdacmacs.client.attribute.data.dto.EncryptedAttributeValueKeyDTO;
import de.tuberlin.tfdacmacs.client.attribute.data.dto.PublicAttributeValueResponse;
import de.tuberlin.tfdacmacs.client.gpp.GPPService;
import de.tuberlin.tfdacmacs.client.keypair.KeyPairService;
import de.tuberlin.tfdacmacs.client.rest.CAClient;
import de.tuberlin.tfdacmacs.client.rest.error.InterServiceCallError;
import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.rsa.AsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.StringSymmetricCryptEngine;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AttributeClient {

    private final CAClient caClient;
    private final KeyPairService keyPairService;
    private final AsymmetricCryptEngine<?> asymmetricCryptEngine;
    private final StringSymmetricCryptEngine symmetricCryptEngine;

    private final GPPService gppService;

    public Set<Attribute> getAttributesForUser(String email, String certificateId) {
        DeviceResponse deviceResponse = caClient.getAttributes(email, certificateId);
        String encryptedKey = deviceResponse.getEncryptedKey();
        Set<EncryptedAttributeValueKeyDTO> encryptedAttributeValueKeys = deviceResponse
                .getEncryptedAttributeValueKeys();

        return decrypt(keyPairService.getKeyPair(email).getPrivateKey(), encryptedKey, encryptedAttributeValueKeys);
    }

    private Set<Attribute> decrypt(PrivateKey privateKey, String encryptedKey, Set<EncryptedAttributeValueKeyDTO> encryptedAttributeValueKeys) {
        try {
            Key key = symmetricCryptEngine.createKeyFromBytes(asymmetricCryptEngine.decryptRaw(encryptedKey, privateKey));

            return encryptedAttributeValueKeys.stream()
                    .map(encryptedAttributeValueKeyDTO -> decrypt(key, encryptedAttributeValueKeyDTO))
                    .collect(Collectors.toSet());
        } catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }

    private Attribute decrypt(Key symmetricKey, EncryptedAttributeValueKeyDTO encryptedAttributeValueKeyDTO) {
        try {
            byte[] byteElement = symmetricCryptEngine.decryptRaw(Base64.decode(encryptedAttributeValueKeyDTO.getEncryptedKey()), symmetricKey);
            Element element = ElementConverter.convert(byteElement, getG1());

            return new Attribute(encryptedAttributeValueKeyDTO.getAttributeValueId(), element);
        } catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }

    private Field getG1() {
        return gppService.getGPP().getPairing().getG1();
    }

    public Optional<AttributeValueKey.Public> findAttributePublicKey(@NonNull String attributeValueId) {
        String[] split = attributeValueId.split(":");
        if(split.length != 2) {
            throw new IllegalArgumentException("Expected attribute value id in the form <aid>:<value> but was: " + attributeValueId);
        }

        try {
            PublicAttributeValueResponse attributeValue = caClient.getAttributeValue(split[0], split[1]);
            return Optional.of(new AttributeValueKey.Public(
                    ElementConverter.convert(attributeValue.getPublicKey(), getG1()),
                    attributeValueId
            ));
        } catch(InterServiceCallError e) {
            if(e.getResponseStatus() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            } else {
                throw e;
            }
        }
    }
}
