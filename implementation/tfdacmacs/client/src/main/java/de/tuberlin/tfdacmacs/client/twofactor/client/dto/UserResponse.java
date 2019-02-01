package de.tuberlin.tfdacmacs.client.twofactor.client.dto;

import de.tuberlin.tfdacmacs.client.attribute.client.dto.DeviceResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    @NotBlank
    private String id;
    @NotBlank
    private String authorityId;

    @NotNull
    private Set<DeviceResponse> devices;
}
