package de.tuberlin.tfdacmacs.lib.db;

import com.couchbase.client.deps.com.fasterxml.jackson.annotation.JsonIgnore;
import com.couchbase.client.java.repository.annotation.Field;
import de.tuberlin.tfdacmacs.lib.events.DomainEvent;
import lombok.*;
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

    @Setter(AccessLevel.NONE)
    @Getter(onMethod = @__({@JsonIgnore, @com.fasterxml.jackson.annotation.JsonIgnore}))
    private transient List<DomainEvent> events = new ArrayList<>();

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
