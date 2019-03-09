package de.tuberlin.tfdacmacs.crypto.pairing.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VersionedID implements Versioned {

    @NotBlank
    private String id;
    @Min(0)
    private long version;

    public VersionedID increment() {
        return new VersionedID(getId(), version + 1);
    }

}
