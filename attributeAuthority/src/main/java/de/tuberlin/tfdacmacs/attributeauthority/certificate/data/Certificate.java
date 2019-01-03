package de.tuberlin.tfdacmacs.attributeauthority.certificate.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.security.cert.X509Certificate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Certificate {

    @NotBlank
    private String id;
    @NotNull
    private X509Certificate certificate;
}
