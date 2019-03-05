package de.tuberlin.tfdacmacs.attributeauthority.user;

import de.tuberlin.tfdacmacs.attributeauthority.attribute.AttributeService;
import de.tuberlin.tfdacmacs.attributeauthority.user.client.UserClient;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.UserAttributeKey;
import de.tuberlin.tfdacmacs.attributeauthority.user.events.DeviceApprovedEvent;
import de.tuberlin.tfdacmacs.attributeauthority.user.events.UserCreatedEvent;
import de.tuberlin.tfdacmacs.crypto.rsa.AsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.StringSymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.SymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import de.tuberlin.tfdacmacs.lib.attributes.data.events.AttributeValueUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final UserClient userClient;
    private final UserService userService;
    private final AttributeService attributeService;

    private final AsymmetricCryptEngine<?> asymmetricCryptEngine = new StringAsymmetricCryptEngine();
    private final SymmetricCryptEngine<?> symmetricCryptEngine = new StringSymmetricCryptEngine();

    @EventListener
    public void handleUserCreatedEvent(UserCreatedEvent userCreatedEvent) {
        userClient.createUserForCA(userCreatedEvent.getSource());
    }


    @EventListener
    public void handleDeviceApprovedEvent(DeviceApprovedEvent deviceApprovedEvent) {
        User user = userService.findUser(deviceApprovedEvent.getSource().getId()).get();

        log.info("Received approval request for user [{}]", user.getId());
        X509Certificate x509Certificate = deviceApprovedEvent.getCertificate().getCertificate();
        PublicKey publicKey = x509Certificate.getPublicKey();

        Set<UserAttributeKey> attributes = user.getAttributes();

        Key aesKey = symmetricCryptEngine.getSymmetricCipherKey();
        String encryptedKey = encryptAsymmetricKey(aesKey, publicKey);
        Map<String, String> encryptedAttributeValueKeys = encryptSymmetricAttributeKeys(attributes, aesKey);

        userClient.updateDeviceForEncryptedAttributeValueKeys(
                user.getId(),
                deviceApprovedEvent.getCertificate().getId(),
                encryptedKey,
                encryptedAttributeValueKeys
        );

        user = userService.extendWithCAUser(user);
        log.info("Successfully encrypted attributes of user [{}] for CA.", user.getId());
    }

    private String encryptAsymmetricKey(Key aesKey, PublicKey publicKey) {
        try {
            return KeyConverter.from(asymmetricCryptEngine.encryptRaw(aesKey.getEncoded(), publicKey)).toBase64();
        } catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> encryptSymmetricAttributeKeys(Set<UserAttributeKey> attributes, Key key) {

        return attributes.stream()
                .collect(Collectors.toMap(
                        attribute -> attribute.getAttributeValueId(),
                        attribute -> {
                            try {
                                return Base64.toBase64String(symmetricCryptEngine.encryptRaw(attribute.getKey().getKey().toBytes(), key));
                            } catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
                                throw new RuntimeException(e);
                            }
                        }
                ));
    }

    @EventListener
    public void handleAttributeValueUpdatedEvent(AttributeValueUpdatedEvent attributeValueUpdatedEvent) {
        String attributeId = attributeValueUpdatedEvent.getSource().getId();
        String value = attributeValueUpdatedEvent.getNewAttributeValue().getValue().toString();

        userService.findUsersByAttributeIdAndValue(attributeId, value)
                .stream()
                .map(User::getId)
                .map(userId -> attributeService.generateUpdateKey(
                        userId,
                        attributeValueUpdatedEvent.getRevokedAttributeValue(),
                        attributeValueUpdatedEvent.getNewAttributeValue()))
                .forEach(updateKey -> userClient.updateUserSecretKey(
                        updateKey,
                        attributeValueUpdatedEvent.getNewAttributeValue().getAttributeValueId(),
                        attributeValueUpdatedEvent.getRevokedAttributeValue().getVersion(),
                        attributeValueUpdatedEvent.getNewAttributeValue().getVersion()));
    }
}
