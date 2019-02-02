package de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.dto;

import de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.EncryptedTwoFactorKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorKeyResponse {

    @NotBlank
    private String id;
    @NotBlank
    private String userId;
    @NotBlank
    private String ownerId;
    @NotNull
    private Map<String, String> encryptedTwoFactorKeys;

    public static TwoFactorKeyResponse from(EncryptedTwoFactorKey encryptedTwoFactorKey) {
        return new TwoFactorKeyResponse(
                encryptedTwoFactorKey.getId(),
                encryptedTwoFactorKey.getUserId(),
                encryptedTwoFactorKey.getDataOwnerId(),
                encryptedTwoFactorKey.getEncryptedTwoFactorKeys()
        );
    }
}
