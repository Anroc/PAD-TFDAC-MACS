package de.tuberlin.tfdacmacs.client.certificate.events;

import org.springframework.context.ApplicationEvent;

public class LoginEvent extends ApplicationEvent {

    public LoginEvent(String email) {
        super(email);
    }

    public String getEmail() {
        return (String) getSource();
    }
}
