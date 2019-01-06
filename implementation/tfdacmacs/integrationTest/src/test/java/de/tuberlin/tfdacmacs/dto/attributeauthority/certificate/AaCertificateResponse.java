package de.tuberlin.tfdacmacs.dto.attributeauthority.certificate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AaCertificateResponse {

    @NotBlank
    private String id;

    @NotBlank
    private String certificate;
}
