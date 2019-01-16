package de.tuberlin.tfdacmacs.client.authority.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeAuthorityResponse {

    public AttributeAuthorityResponse(@NotBlank String id,
            @NotBlank String certificateId) {
        this.id = id;
        this.certificateId = certificateId;
    }

    @NotBlank
    private String id;
    @NotBlank
    private String certificateId;

    @Nullable
    private String publicKey;
}
