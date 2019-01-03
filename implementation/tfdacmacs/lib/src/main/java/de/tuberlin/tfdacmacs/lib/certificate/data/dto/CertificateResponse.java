package de.tuberlin.tfdacmacs.lib.certificate.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateResponse {

    @NotBlank
    private String id;

    @NotBlank
    private String certificate;
}
