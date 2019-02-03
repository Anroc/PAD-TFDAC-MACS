package de.tuberlin.tfdacmacs.client.rest.session.events;

import de.tuberlin.tfdacmacs.client.rest.session.Session;

public class SessionDestroyedEvent extends SessionEvent {
    public SessionDestroyedEvent(Session source) {
        super(source);
    }
}
