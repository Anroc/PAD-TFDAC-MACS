package de.tuberlin.tfdacmacs.basics.user.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationRequest {

    @Email
    @NotBlank
    private String email;
}
