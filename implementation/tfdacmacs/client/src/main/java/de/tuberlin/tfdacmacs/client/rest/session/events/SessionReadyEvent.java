package de.tuberlin.tfdacmacs.client.rest.session.events;

import de.tuberlin.tfdacmacs.client.rest.session.Session;

public class SessionReadyEvent extends SessionEvent {
    public SessionReadyEvent(Session source) {
        super(source);
    }
}
