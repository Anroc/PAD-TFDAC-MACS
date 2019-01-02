package de.tuberlin.tfdacmacs.lib.user.data.dto;

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
