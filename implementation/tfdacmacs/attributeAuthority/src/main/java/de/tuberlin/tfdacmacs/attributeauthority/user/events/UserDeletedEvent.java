package de.tuberlin.tfdacmacs.attributeauthority.user.events;

import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
import de.tuberlin.tfdacmacs.lib.events.DomainEvent;
import lombok.NonNull;

public class UserDeletedEvent extends DomainEvent<User> {
    public UserDeletedEvent(@NonNull User user) {
        super(user);
    }
}
