package de.tuberlin.tfdacmacs.lib.authority;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;

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
    @Nullable
    private Long version;
    @Nullable
    private String signature;
}
