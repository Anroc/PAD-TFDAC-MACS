package de.tuberlin.tfdacmacs.dto.centralauthority.certificate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaCertificateResponse {

    @NotBlank
    private String id;

    @NotBlank
    private String certificate;
}
