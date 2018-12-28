package de.tuberlin.tfdacmacs.basics.db;

import com.couchbase.client.java.repository.annotation.Field;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

import java.util.UUID;

@Data
@Document
public abstract class Entity {

    @Id
    @Field
    private String id;

    public Entity() {
        this.id = UUID.randomUUID().toString();
    }

    public Entity(@NonNull String id) {
        this.id = id;
    }

}
