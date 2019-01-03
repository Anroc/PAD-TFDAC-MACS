package de.tuberlin.tfdacmacs.attributeauthority.init.authority.events;

import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AuthorityKey;
import de.tuberlin.tfdacmacs.lib.events.DomainEvent;

public class AuthorityKeyCreatedEvent extends DomainEvent<AuthorityKey> {
    public AuthorityKeyCreatedEvent(AuthorityKey source) {
        super(source);
    }
}
