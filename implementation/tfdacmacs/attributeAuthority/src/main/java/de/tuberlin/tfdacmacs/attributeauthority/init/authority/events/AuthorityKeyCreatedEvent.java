package de.tuberlin.tfdacmacs.attributeauthority.init.authority.events;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.Key;
import de.tuberlin.tfdacmacs.basics.events.DomainEvent;

public class AuthorityKeyCreatedEvent extends DomainEvent<Key> {
    public AuthorityKeyCreatedEvent(Key source) {
        super(source);
    }
}
