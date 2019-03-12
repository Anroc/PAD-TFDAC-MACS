package de.tuberlin.tfdacmacs.attributeauthority.ciphertext;

import de.tuberlin.tfdacmacs.lib.attributes.data.events.AttributeValueUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CipherTextEventListener {

    private final CipherTextService cipherTextService;

    @EventListener
    public void handleAttributeValueUpdatedEvent(AttributeValueUpdatedEvent attributeValueUpdatedEvent) {
        cipherTextService.updateCipherTexts(
                attributeValueUpdatedEvent.getRevokedAttributeValue(),
                attributeValueUpdatedEvent.getNewAttributeValue()
        );
    }
}
