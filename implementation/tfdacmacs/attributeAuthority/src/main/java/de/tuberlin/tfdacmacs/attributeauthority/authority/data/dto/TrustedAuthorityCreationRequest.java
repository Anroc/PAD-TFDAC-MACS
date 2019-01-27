package de.tuberlin.tfdacmacs.attributeauthority.authority.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrustedAuthorityCreationRequest {

    @NotBlank
    private String id;

    @NotBlank
    private String certificateId;
}
