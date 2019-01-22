package de.tuberlin.tfdacmacs.client.config;

import lombok.NonNull;

public interface StandardStreams {

    void out(@NonNull String message);

    void error(@NonNull String message);
}
