package de.tuberlin.tfdacmacs.crypto.pairing.data;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
public class File {

    @NotNull
    private final String id = UUID.randomUUID().toString();

    private final byte[] data;

    public File(byte[] data) {
        this.data = data;
    }
}
