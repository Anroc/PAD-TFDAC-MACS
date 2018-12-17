package de.tuberlin.tfdacmacs.attributeauthority.init.authority.events;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.AuthorityKey;
import de.tuberlin.tfdacmacs.basics.events.DomainEvent;

public class AuthorityKeyCreatedEvent extends DomainEvent<AuthorityKey> {
    public AuthorityKeyCreatedEvent(AuthorityKey source) {
        super(source);
    }
}
