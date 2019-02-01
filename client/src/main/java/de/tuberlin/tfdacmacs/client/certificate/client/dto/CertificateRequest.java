package de.tuberlin.tfdacmacs.client.certificate.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRequest {

    @NotBlank
    private String certificateRequest;
}

