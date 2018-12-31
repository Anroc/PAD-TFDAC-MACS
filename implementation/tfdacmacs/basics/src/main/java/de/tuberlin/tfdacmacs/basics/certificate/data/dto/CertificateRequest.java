package de.tuberlin.tfdacmacs.basics.certificate.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRequest {

    @NotBlank
    private String publicKey;

    @NotBlank
    private String certificateRequest;
}
