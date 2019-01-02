package de.tuberlin.tfdacmacs.lib.authority;

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
}
