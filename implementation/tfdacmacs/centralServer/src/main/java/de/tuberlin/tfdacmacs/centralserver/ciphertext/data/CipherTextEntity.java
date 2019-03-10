package de.tuberlin.tfdacmacs.centralserver.ciphertext.data;

import de.tuberlin.tfdacmacs.crypto.pairing.data.CipherText;
import de.tuberlin.tfdacmacs.crypto.pairing.data.VersionedID;
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
            Set<VersionedID> accessPolicy, VersionedID ownerId, String fileId) {
        super(id);
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        this.accessPolicy = accessPolicy;
        this.ownerId = ownerId;
        this.fileId = fileId;
    }

    @NotNull
    private Element c1;
    @NotNull
    private Element c2;
    @NotNull
    private Element c3;

    @NotEmpty
    private Set<VersionedID> accessPolicy;

    private VersionedID ownerId;

    @NotBlank
    private String fileId;

    public CipherText toCipherText() {
        return new CipherText(
                getId(), getC1(), getC2(), getC3(), getAccessPolicy(), getOwnerId(), getFileId()
        );
    }

    public static CipherTextEntity from(@NonNull CipherText cipherText) {
        return new CipherTextEntity(
                cipherText.getId(),
                cipherText.getC1(),
                cipherText.getC2(),
                cipherText.getC3(),
                cipherText.getAccessPolicy(),
                cipherText.getOwnerId(),
                cipherText.getFileId()
        );
    }
}
