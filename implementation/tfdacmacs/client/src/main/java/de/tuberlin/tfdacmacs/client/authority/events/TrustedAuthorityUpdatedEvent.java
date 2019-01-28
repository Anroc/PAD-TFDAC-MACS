package de.tuberlin.tfdacmacs.client.authority.events;

import de.tuberlin.tfdacmacs.client.authority.data.TrustedAuthority;
import org.springframework.context.ApplicationEvent;

public class TrustedAuthorityUpdatedEvent extends ApplicationEvent {

    public TrustedAuthorityUpdatedEvent(TrustedAuthority source) {
        super(source);
    }

    public TrustedAuthority getSource() {
        return (TrustedAuthority) super.getSource();
    }
}
