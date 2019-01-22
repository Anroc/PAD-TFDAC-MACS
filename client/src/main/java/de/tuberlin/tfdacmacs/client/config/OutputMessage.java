package de.tuberlin.tfdacmacs.client.config;

import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class OutputMessage implements StandardStreams {
    @Override
    public void out(@NonNull String message) {
        System.out.println(message);
    }

    @Override
    public void error(@NonNull String message) {
        System.err.println(message);
    }
}
