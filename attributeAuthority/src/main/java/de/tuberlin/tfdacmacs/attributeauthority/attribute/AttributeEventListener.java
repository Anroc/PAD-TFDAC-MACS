package de.tuberlin.tfdacmacs.attributeauthority.attribute;

import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
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

        user.getAttributes()
                .forEach(userAttributeKey -> attributeService.findAttribute(userAttributeKey.getAttributeId())
                        .ifPresent(attribute -> attributeService.revoke(attribute, userAttributeKey.getAttributeValueId()))
                );
    }
}
