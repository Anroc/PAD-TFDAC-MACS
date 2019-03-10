package de.tuberlin.tfdacmacs.crypto.pairing.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class VersionedID implements Versioned {

    @NotBlank
    private String id;
    @Min(0)
    private long version;

    public VersionedID(@NonNull String id, long version) {
        if(version < 0L) {
            throw new IllegalArgumentException("Version is marked with @Min(0)");
        }

        this.id = id;
        this.version = version;
    }

    public VersionedID increment() {
        return new VersionedID(getId(), version + 1);
    }

}
