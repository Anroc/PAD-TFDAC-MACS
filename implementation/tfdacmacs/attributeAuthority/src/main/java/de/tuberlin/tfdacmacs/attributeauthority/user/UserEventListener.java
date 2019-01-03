package de.tuberlin.tfdacmacs.attributeauthority.user;

import de.tuberlin.tfdacmacs.attributeauthority.user.client.UserClient;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.UserAttributeKey;
import de.tuberlin.tfdacmacs.attributeauthority.user.events.DeviceApprovedEvent;
import de.tuberlin.tfdacmacs.attributeauthority.user.events.UserCreatedEvent;
import de.tuberlin.tfdacmacs.crypto.rsa.AsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
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

    @EventListener
    public void handleUserCreatedEvent(UserCreatedEvent userCreatedEvent) {
        userClient.createUserForCA(userCreatedEvent.getSource());
    }


    @EventListener
    public void handleDeviceApprovedEvent(DeviceApprovedEvent deviceApprovedEvent) {
        User user = deviceApprovedEvent.getSource();
        log.info("Received approval request for user [{}]", user.getId());
        X509Certificate x509Certificate = deviceApprovedEvent.getCertificate().getCertificate();
        PublicKey publicKey = x509Certificate.getPublicKey();

        Set<UserAttributeKey> attributes = user.getAttributes();
        Map<String, String> encryptedAttributeValueKeys = encryptAttributeKeys(attributes, publicKey);

        userClient.updateDeviceForEncryptedAttributeValueKeys(
                user.getId(),
                deviceApprovedEvent.getCertificate().getId(),
                encryptedAttributeValueKeys
        );
        log.info("Successfully encrypted attributes of user [{}] for CA.", user.getId());
    }

    private Map<String, String> encryptAttributeKeys(Set<UserAttributeKey> attributes, PublicKey publicKey) {
        AsymmetricCryptEngine<?> asymmetricCryptEngine = new StringAsymmetricCryptEngine();
        return attributes.stream()
                .collect(Collectors.toMap(
                        attribute -> attribute.getAttributeValueId(),
                        attribute -> {
                            try {
                                return asymmetricCryptEngine.encryptRaw(attribute.getKey().getSecretKey().toBytes(), publicKey);
                            } catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
                                throw new RuntimeException(e);
                            }
                        }
                ));
    }
}
