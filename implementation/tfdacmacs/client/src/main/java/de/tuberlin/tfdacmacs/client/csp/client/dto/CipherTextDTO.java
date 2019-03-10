package de.tuberlin.tfdacmacs.client.csp.client.dto;

import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.CipherText;
import de.tuberlin.tfdacmacs.crypto.pairing.data.VersionedID;
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
    private Set<VersionedID> accessPolicy;
    @Nullable
    private VersionedID ownerId;

    @NotBlank
    private String fileId;

    public CipherText toCipherText(@NonNull Field g1, @NonNull Field gt) {
        return new CipherText(
                id,
                ElementConverter.convert(c1, gt),
                ElementConverter.convert(c2, g1),
                ElementConverter.convert(c3, g1),
                new HashSet<>(accessPolicy),
                ownerId,
                fileId
        );
    }

    public static CipherTextDTO from(@NonNull CipherText cipherText) {
        return new CipherTextDTO(
                cipherText.getId(),
                ElementConverter.convert(cipherText.getC1()),
                ElementConverter.convert(cipherText.getC2()),
                ElementConverter.convert(cipherText.getC3()),
                cipherText.getAccessPolicy(),
                cipherText.getOwnerId(),
                cipherText.getFileId()
        );
    }

}
