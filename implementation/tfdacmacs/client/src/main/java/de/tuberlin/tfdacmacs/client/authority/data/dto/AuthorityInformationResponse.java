package de.tuberlin.tfdacmacs.client.authority.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorityInformationResponse {
    @NotBlank
    private String id;
    @NotBlank
    private String certificateId;
    @NotNull
    private Map<String, String> trustedAuthorityIds;
}
