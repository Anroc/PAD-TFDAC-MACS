package de.tuberlin.tfdacmacs.attributeauthority.attribute.client;

import de.tuberlin.tfdacmacs.attributeauthority.client.CAClient;
import de.tuberlin.tfdacmacs.lib.attributes.data.AttributeValueComponent;
import de.tuberlin.tfdacmacs.lib.attributes.data.dto.AttributeCreationRequest;
import de.tuberlin.tfdacmacs.lib.attributes.data.dto.AttributeValueCreationRequest;
import de.tuberlin.tfdacmacs.lib.attributes.data.events.AttributeCreatedEvent;
import de.tuberlin.tfdacmacs.lib.attributes.data.events.AttributeValueCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttributeClient {

    private final CAClient caClient;

    @EventListener
    public void createAttribute(AttributeCreatedEvent attributeEvent) {
        AttributeCreationRequest attributeCreationRequest = AttributeCreationRequest.from(attributeEvent.getSource());

        caClient.createAttribute(attributeCreationRequest);
    }

    @EventListener
    public void createAttributeValue(AttributeValueCreatedEvent attributeValueCreatedEvent) {
        AttributeValueComponent value = attributeValueCreatedEvent.getValue();
        AttributeValueCreationRequest attributeValueCreationRequest = AttributeValueCreationRequest.from(value);

        caClient.createAttributeValue(attributeValueCreatedEvent.getSource().getId(), attributeValueCreationRequest);
    }
}
