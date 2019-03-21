package de.tuberlin.tfdacmacs.crypto.benchmark;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public abstract class User {

    private final String id;

    public User() {
        this.id = UUID.randomUUID().toString();
    }



}
