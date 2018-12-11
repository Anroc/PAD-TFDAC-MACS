package de.tuberlin.tfdacmacs.centralserver.user.data;

import de.tuberlin.tfdacmacs.basics.db.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(callSuper = true)
public class User extends Entity {

    public User(@NonNull String id, @NotBlank String idSignature) {
        super(id);
        this.idSignature = idSignature;
    }

    @NotBlank
    private String idSignature;
}
