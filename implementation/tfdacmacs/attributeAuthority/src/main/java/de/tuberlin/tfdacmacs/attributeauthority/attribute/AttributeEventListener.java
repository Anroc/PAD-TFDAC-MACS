package de.tuberlin.tfdacmacs.attributeauthority.attribute;

import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
import de.tuberlin.tfdacmacs.attributeauthority.user.events.UserAttributeValueRevokedEvent;
import de.tuberlin.tfdacmacs.attributeauthority.user.events.UserDeletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttributeEventListener {

    private final AttributeService attributeService;

    @EventListener
    public void revokeAttributes(UserDeletedEvent userDeletedEvent) {
        User user = userDeletedEvent.getSource();

        user.getAttributes().forEach(userAttributeKey -> revokeAttribute(userAttributeKey.getAttributeId(), userAttributeKey.getAttributeValueId()));
    }

    @EventListener
    public void revokeAttribute(UserAttributeValueRevokedEvent userAttributeValueRevokedEvent) {
        String attributeId = userAttributeValueRevokedEvent.getUserAttributeKey().getAttributeId();
        revokeAttribute(attributeId, userAttributeValueRevokedEvent.getUserAttributeKey().getAttributeValueId());
    }

    private void revokeAttribute(String attributeId, String attributeValueId) {
        attributeService.findAttribute(attributeId)
                .ifPresent(attribute -> attributeService.revoke(attribute, attributeValueId));
    }
}
