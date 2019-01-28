package de.tuberlin.tfdacmacs.lib.authority;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeAuthorityPublicKeyRequest {

    @NotBlank
    private String authorityPublicKey;

    @NotBlank
    private String signature;
}
