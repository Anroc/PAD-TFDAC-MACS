package de.tuberlin.tfdacmacs.client.authority.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.security.cert.X509Certificate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrustedAuthority {
    @NotBlank
    private String id;
    @NotBlank
    private String certificateId;
    @NotNull
    private X509Certificate certificate;
}
