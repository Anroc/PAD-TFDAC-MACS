package de.tuberlin.tfdacmacs.lib.authority;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeAuthorityPublicKeyRequest {

    @NotBlank
    private String authorityPublicKey;

    @Min(0)
    private long version;

    @NotBlank
    // authorityPublicKey + version
    private String signature;
}
