package de.tuberlin.tfdacmacs;

import lombok.Data;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Component
public class TestEventListener {
    private boolean record = false;
    private List<ApplicationEvent> applicationEvents = new ArrayList<>();

    @EventListener(ApplicationEvent.class)
    public void eventListener(ApplicationEvent applicationEvent) {
        if(record) {
            applicationEvents.add(applicationEvent);
        }
    }

    public void recordEvents() {
        record = true;
        this.applicationEvents.clear();
    }

    public List<ApplicationEvent> stopRecodingEvents() {
        record = false;
        return this.applicationEvents;
    }

    public <T> List<T> findEventOfType(Class<T> clazz) {
        return this.applicationEvents.stream()
                .filter(applicationEvent -> clazz.isAssignableFrom(applicationEvent.getClass()))
                .map(applicationEvent -> (T) applicationEvent)
                .collect(Collectors.toList());
    }
}