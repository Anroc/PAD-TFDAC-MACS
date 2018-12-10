package de.tuberlin.tfdacmacs.attributeauthority.user.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank
    private String email;

    @NotBlank
    private String publicKey;

    @NotEmpty
    private Map<String, Object> attributes;
}
