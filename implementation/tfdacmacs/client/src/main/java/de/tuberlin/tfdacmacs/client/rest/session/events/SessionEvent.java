package de.tuberlin.tfdacmacs.client.rest.session.events;

import de.tuberlin.tfdacmacs.client.rest.session.Session;
import org.springframework.context.ApplicationEvent;

public class SessionEvent extends ApplicationEvent {

    public SessionEvent(Session source) {
        super(source);
    }

    @Override
    public Session getSource() {
        return (Session) super.getSource();
    }
}
