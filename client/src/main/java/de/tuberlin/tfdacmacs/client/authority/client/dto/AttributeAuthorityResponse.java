package de.tuberlin.tfdacmacs.client.authority.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @NotBlank
    private String signature;
}
