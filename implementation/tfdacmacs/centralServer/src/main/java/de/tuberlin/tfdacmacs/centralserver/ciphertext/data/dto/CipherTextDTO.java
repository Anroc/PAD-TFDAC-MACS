package de.tuberlin.tfdacmacs.centralserver.ciphertext.data.dto;

import de.tuberlin.tfdacmacs.centralserver.ciphertext.data.CipherTextEntity;
import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import it.unisa.dia.gas.jpbc.Field;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CipherTextDTO {

    @NotBlank
    private String id;

    @NotBlank
    private String c1;
    @NotBlank
    private String c2;
    @NotBlank
    private String c3;

    @NotEmpty
    private Set<String> accessPolicy;
    @Nullable
    private String ownerId;

    @NotBlank
    private String fileId;

    public CipherTextEntity toCipherTextEntity(@NonNull Field g1, @NonNull Field gt) {
        return new CipherTextEntity(
                id,
                ElementConverter.convert(c1, gt),
                ElementConverter.convert(c2, g1),
                ElementConverter.convert(c3, g1),
                new HashSet<>(accessPolicy),
                ownerId,
                fileId
        );
    }

    public static CipherTextDTO from(@NonNull CipherTextEntity cipherTextEntity) {
        return new CipherTextDTO(
                cipherTextEntity.getId(),
                ElementConverter.convert(cipherTextEntity.getC1()),
                ElementConverter.convert(cipherTextEntity.getC2()),
                ElementConverter.convert(cipherTextEntity.getC3()),
                cipherTextEntity.getAccessPolicy(),
                cipherTextEntity.getOwnerId(),
                cipherTextEntity.getFileId()
        );
    }
}
