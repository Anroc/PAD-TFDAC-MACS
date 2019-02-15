package de.tuberlin.tfdacmacs.client.twofactor.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwoFactorUpdateKeyRequest {

    private String updateKey;

}
