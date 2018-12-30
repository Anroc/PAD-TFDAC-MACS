package de.tuberlin.tfdacmacs.centralserver.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateResponse {

    @NotBlank
    private String certificate;
}
