package de.tuberlin.tfdacmacs.lib.ciphertext.data.dto;

import de.tuberlin.tfdacmacs.crypto.pairing.data.VersionedID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttributeCipherTextUpdateKeyDTO {

    @Nullable
    private VersionedID dataOwnerId;

    @NotBlank
    private String updateKey;

}
