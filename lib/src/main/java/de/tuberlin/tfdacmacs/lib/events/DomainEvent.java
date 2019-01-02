package de.tuberlin.tfdacmacs.lib.events;

import org.springframework.context.ApplicationEvent;

public class DomainEvent<T> extends ApplicationEvent {

    public DomainEvent(T source) {
        super(source);
    }

    @Override
    public T getSource() {
        return (T) super.getSource();
    }
}
