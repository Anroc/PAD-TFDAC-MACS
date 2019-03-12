package de.tuberlin.tfdacmacs.lib.ciphertext.data.dto;

import de.tuberlin.tfdacmacs.crypto.pairing.data.VersionedID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttributeCipherTextUpdateKeyDTO {

    @NotNull
    private VersionedID dataOwnerId;

    @NotBlank
    private String updateKey;

}
