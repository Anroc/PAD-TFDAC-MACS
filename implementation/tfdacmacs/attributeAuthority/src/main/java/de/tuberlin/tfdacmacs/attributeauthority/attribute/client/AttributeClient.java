package de.tuberlin.tfdacmacs.attributeauthority.attribute.client;

import de.tuberlin.tfdacmacs.attributeauthority.client.CAClient;
import de.tuberlin.tfdacmacs.attributeauthority.keypair.KeyPairService;
import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.lib.attributes.data.AttributeValueComponent;
import de.tuberlin.tfdacmacs.lib.attributes.data.dto.AttributeCreationRequest;
import de.tuberlin.tfdacmacs.lib.attributes.data.dto.AttributeValueCreationRequest;
import de.tuberlin.tfdacmacs.lib.attributes.data.events.AttributeCreatedEvent;
import de.tuberlin.tfdacmacs.lib.attributes.data.events.AttributeValueCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;

@Component
@RequiredArgsConstructor
public class AttributeClient {

    private final CAClient caClient;
    private final KeyPairService keyPairService;
    private final StringAsymmetricCryptEngine stringAsymmetricCryptEngine;

    @EventListener
    public void createAttribute(AttributeCreatedEvent attributeEvent) {
        AttributeCreationRequest attributeCreationRequest = AttributeCreationRequest.from(
                attributeEvent.getSource(),
                this::sign
        );

        caClient.createAttribute(attributeCreationRequest);
    }

    @EventListener
    public void createAttributeValue(AttributeValueCreatedEvent attributeValueCreatedEvent) {
        AttributeValueComponent value = attributeValueCreatedEvent.getValue();

        AttributeValueCreationRequest attributeValueCreationRequest = AttributeValueCreationRequest.from(
                value,
                sign(value)
        );

        caClient.createAttributeValue(attributeValueCreatedEvent.getSource().getId(), attributeValueCreationRequest);
    }

    private String sign(AttributeValueComponent value) {
        try {
            return stringAsymmetricCryptEngine.sign(value.buildSignatureBody(), keyPairService.getPrivateKey());
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
