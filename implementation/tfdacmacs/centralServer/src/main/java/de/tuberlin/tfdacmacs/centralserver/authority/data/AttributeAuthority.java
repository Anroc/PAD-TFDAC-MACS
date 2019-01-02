package de.tuberlin.tfdacmacs.centralserver.authority.data;

import de.tuberlin.tfdacmacs.lib.db.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class AttributeAuthority extends Entity {

    @NotBlank
    private String certificateId;

    public AttributeAuthority(@NonNull String id, @NonNull String certificateId) {
        super(id);
        this.certificateId = certificateId;
    }
}
