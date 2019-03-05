package de.tuberlin.tfdacmacs.attributeauthority.user.events;

import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.UserAttributeKey;
import de.tuberlin.tfdacmacs.lib.events.DomainEvent;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class UserAttributeValueRevokedEvent extends DomainEvent<User> {

    private final UserAttributeKey userAttributeKey;

    public UserAttributeValueRevokedEvent(@NonNull User user, @NonNull UserAttributeKey userAttributeKey) {
        super(user);
        this.userAttributeKey = userAttributeKey;
    }
}
