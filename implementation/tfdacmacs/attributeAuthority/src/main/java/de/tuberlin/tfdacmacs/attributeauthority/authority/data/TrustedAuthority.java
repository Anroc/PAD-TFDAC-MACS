package de.tuberlin.tfdacmacs.attributeauthority.authority.data;

import de.tuberlin.tfdacmacs.lib.db.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TrustedAuthority extends Entity {

    @NotBlank
    private String certificateId;

    public TrustedAuthority(@NonNull String id, @NonNull String certificateId) {
        super(id);
        this.certificateId = certificateId;
    }
}
