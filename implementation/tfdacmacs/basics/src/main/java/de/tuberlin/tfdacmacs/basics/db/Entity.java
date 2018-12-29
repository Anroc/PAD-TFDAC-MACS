package de.tuberlin.tfdacmacs.basics.db;

import com.couchbase.client.java.repository.annotation.Field;
import de.tuberlin.tfdacmacs.basics.events.DomainEvent;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Document
public abstract class Entity {

    @Id
    @Field
    private String id;

    private transient final List<DomainEvent> events = new ArrayList<>();

    public Entity() {
        this.id = UUID.randomUUID().toString();
    }

    public Entity(@NonNull String id) {
        this.id = id;
    }

    public <T extends DomainEvent> Entity registerDomainEvent(T event) {
        events.add(event);
        return this;
    }

}
