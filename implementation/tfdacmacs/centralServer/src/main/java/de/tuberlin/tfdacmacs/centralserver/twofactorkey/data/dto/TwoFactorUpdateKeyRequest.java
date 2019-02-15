package de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwoFactorUpdateKeyRequest {

    private String updateKey;

}
