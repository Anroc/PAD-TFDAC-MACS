package de.tuberlin.tfdacmacs.csp.ciphertext.data;

import de.tuberlin.tfdacmacs.lib.db.Entity;
import it.unisa.dia.gas.jpbc.Element;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CipherTextEntity extends Entity {

    public CipherTextEntity(@NonNull String id, Element c1, Element c2, Element c3,
            Set<String> accessPolicy, String ownerId, String encryptedMessage) {
        super(id);
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        this.accessPolicy = accessPolicy;
        this.ownerId = ownerId;
        this.encryptedMessage = encryptedMessage;
    }

    @NotNull
    private Element c1;
    @NotNull
    private Element c2;
    @NotNull
    private Element c3;

    @NotEmpty
    private Set<String> accessPolicy;

    private String ownerId;

    @NotBlank
    private String encryptedMessage;
}
