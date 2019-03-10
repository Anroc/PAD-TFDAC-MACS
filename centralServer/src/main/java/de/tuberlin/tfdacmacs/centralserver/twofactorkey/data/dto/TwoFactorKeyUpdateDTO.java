package de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwoFactorKeyUpdateDTO {

    @Min(0)
    private long targetVersion;
    @NotBlank
    private String updateKey;
}
