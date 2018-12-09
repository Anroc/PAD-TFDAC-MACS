package de.tuberlin.tfdacmacs.basics.db;

import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

@Data
public abstract class Entity {

    private String id;

    public Entity() {
        this.id = UUID.randomUUID().toString();
    }

    public Entity(@NonNull String id) {
        this.id = id;
    }
}
