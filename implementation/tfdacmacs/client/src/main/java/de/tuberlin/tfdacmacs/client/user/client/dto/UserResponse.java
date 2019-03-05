package de.tuberlin.tfdacmacs.client.user.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    @NotBlank
    private String id;
    @NotBlank
    private String authorityId;

    @Valid
    @Nullable
    private TwoFactorPublicKeyDTO twoFactorPublicKey;

    @Valid
    @Nullable
    private Map<String, Map<Long, AttributeValueUpdateKeyDTO>> attributeValueUpdateKeys;

    @Valid
    @NotNull
    private Set<DeviceResponse> devices;
}
