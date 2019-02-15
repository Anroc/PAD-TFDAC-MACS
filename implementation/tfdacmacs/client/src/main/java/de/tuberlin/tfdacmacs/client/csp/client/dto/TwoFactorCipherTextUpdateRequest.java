package de.tuberlin.tfdacmacs.client.csp.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwoFactorCipherTextUpdateRequest {

    @NotBlank
    private String ownerId;

    @NotNull
    @NotEmpty
    private List<TwoFactorCipherTextUpdateKey> updates;

}
