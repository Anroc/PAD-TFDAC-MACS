package de.tuberlin.tfdacmacs.client.authority.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeAuthorityResponse {

    @NotBlank
    private String id;
    @NotBlank
    private String certificateId;

    @NotBlank
    private String publicKey;
    @Min(0)
    private long version;

    @NotBlank
    // authorityPublicKey + version
    private String signature;
}
