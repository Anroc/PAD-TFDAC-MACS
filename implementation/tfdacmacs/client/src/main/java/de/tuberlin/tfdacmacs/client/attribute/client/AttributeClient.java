package de.tuberlin.tfdacmacs.client.attribute.client;

import de.tuberlin.tfdacmacs.client.attribute.client.dto.PublicAttributeValueResponse;
import de.tuberlin.tfdacmacs.client.attribute.data.Attribute;
import de.tuberlin.tfdacmacs.client.gpp.GPPService;
import de.tuberlin.tfdacmacs.client.keypair.KeyPairService;
import de.tuberlin.tfdacmacs.client.rest.CAClient;
import de.tuberlin.tfdacmacs.client.rest.SemanticValidator;
import de.tuberlin.tfdacmacs.client.rest.error.InterServiceCallError;
import de.tuberlin.tfdacmacs.client.user.client.dto.AttributeValueUpdateKeyDTO;
import de.tuberlin.tfdacmacs.client.user.client.dto.DeviceResponse;
import de.tuberlin.tfdacmacs.client.user.client.dto.EncryptedAttributeValueKeyDTO;
import de.tuberlin.tfdacmacs.client.user.client.dto.UserResponse;
import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.UserAttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.UserAttributeValueUpdateKey;
import de.tuberlin.tfdacmacs.crypto.pairing.util.AttributeValueId;
import de.tuberlin.tfdacmacs.crypto.rsa.AsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.StringSymmetricCryptEngine;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttributeClient {

    private final CAClient caClient;
    private final KeyPairService keyPairService;
    private final AsymmetricCryptEngine<?> asymmetricCryptEngine;
    private final StringSymmetricCryptEngine symmetricCryptEngine;
    private final SemanticValidator semanticValidator;

    private final GPPService gppService;

    public Set<Attribute> getAttributesForUser(String email, String certificateId) {
        UserResponse userResponse = caClient.getUser(email);

        DeviceResponse deviceResponse = userResponse.getDevices()
                .stream().filter(dr -> dr.getCertificateId().equals(certificateId))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("This device does not exist anymore."));

        String encryptedKey = deviceResponse.getEncryptedKey();
        Set<EncryptedAttributeValueKeyDTO> encryptedAttributeValueKeys = deviceResponse
                .getEncryptedAttributeValueKeys();

        Set<Attribute> attributes = decrypt(keyPairService.getKeyPair(email).getPrivateKey(), encryptedKey,
                encryptedAttributeValueKeys);

        updateAttributes(userResponse, attributes);
        return attributes;
    }

    private void updateAttributes(UserResponse userResponse, Set<Attribute> attributes) {
        for (Attribute attribute : attributes) {
            if(userResponse.getAttributeValueUpdateKeys().containsKey(attribute.getId())) {
                Map<Long, AttributeValueUpdateKeyDTO> attributeValueUpdateKeyDTOs = userResponse.getAttributeValueUpdateKeys().get(attribute.getId());
                long currentVersion = attribute.getUserAttributeValueKey().getVersion();
                while(attributeValueUpdateKeyDTOs.containsKey(currentVersion)) {
                    AttributeValueUpdateKeyDTO attributeValueUpdateKeyDTO = attributeValueUpdateKeyDTOs.get(currentVersion);

                    semanticValidator.verifySignature(attributeValueUpdateKeyDTO.buildSignatureBody(), attributeValueUpdateKeyDTO.getSignature(), userResponse.getAuthorityId());

                    log.info("Found new version of attribute {} to update to version {}", attribute.getId(), attributeValueUpdateKeyDTO.getTargetVersion() + 1);
                    UserAttributeValueUpdateKey userAttributeValueUpdateKey = new UserAttributeValueUpdateKey(
                            userResponse.getId(),
                            ElementConverter.convert(attributeValueUpdateKeyDTO.getUpdateKey(), getG1()),
                            attributeValueUpdateKeyDTO.getTargetVersion()
                    );
                    attribute.getUserAttributeValueKey().update(userAttributeValueUpdateKey);
                    currentVersion = attribute.getUserAttributeValueKey().getVersion();
                }
            }
        }
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

            return new Attribute(
                    encryptedAttributeValueKeyDTO.getAttributeValueId(),
                    new UserAttributeValueKey(element, encryptedAttributeValueKeyDTO.getVersion()));
        } catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }

    private Field getG1() {
        return gppService.getGPP().g1();
    }

    public Optional<AttributeValueKey.Public> findAttributePublicKey(@NonNull String attributeValueId) {
        AttributeValueId attrValueId =  new AttributeValueId(attributeValueId);

        try {
            PublicAttributeValueResponse attributeValueResponse = caClient.getAttributeValue(attrValueId.getAttributeId(), attrValueId.getValue());

            String signatureContent = attributeValueResponse.getValue().toString()
                    + ";" + attributeValueResponse.getPublicKey()
                    + ";" + attributeValueResponse.getVersion();
            semanticValidator.verifySignature(signatureContent, attributeValueResponse.getSignature(), attrValueId.getAuthorityId());

            return Optional.of(new AttributeValueKey.Public(
                    ElementConverter.convert(attributeValueResponse.getPublicKey(), getG1()),
                    attributeValueId,
                    attributeValueResponse.getVersion()
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
