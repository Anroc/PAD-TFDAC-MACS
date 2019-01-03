package de.tuberlin.tfdacmacs.attributeauthority.user.events;

import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
import de.tuberlin.tfdacmacs.lib.events.DomainEvent;

public class UserCreatedEvent extends DomainEvent<User> {
    public UserCreatedEvent(User source) {
        super(source);
    }
}
