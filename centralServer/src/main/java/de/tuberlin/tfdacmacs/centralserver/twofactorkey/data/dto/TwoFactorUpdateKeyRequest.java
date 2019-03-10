package de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwoFactorUpdateKeyRequest {

    @NotBlank
    private String updateKey;

    @Min(0)
    private long targetVersion;

}
