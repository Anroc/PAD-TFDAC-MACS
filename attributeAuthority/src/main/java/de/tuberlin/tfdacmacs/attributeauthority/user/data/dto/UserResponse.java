package de.tuberlin.tfdacmacs.attributeauthority.user.data.dto;

import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    @NotBlank
    private String id;

    @NotEmpty
    private Set<AttributeValueResponse> attributes;

    public static UserResponse from(@NonNull User user) {
        return new UserResponse(
                user.getId(),
                user.getAttributes().stream().map(AttributeValueResponse::from).collect(Collectors.toSet())
        );
    }
}
