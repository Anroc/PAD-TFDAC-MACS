package de.tuberlin.tfdacmacs.centralserver.user.data.dto;

import de.tuberlin.tfdacmacs.centralserver.user.data.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationResponse {

    @NotBlank
    private String id;

    @NotBlank
    private String idSignature;

    public static UserCreationResponse from(@NonNull User user) {
        return new UserCreationResponse(
                user.getId(),
                user.getIdSignature()
        );
    }
}
