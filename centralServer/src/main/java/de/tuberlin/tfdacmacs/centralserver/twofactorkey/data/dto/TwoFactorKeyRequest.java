package de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.dto;

import de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.EncryptedTwoFactorKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorKeyRequest {

    @NotBlank
    private String userId;
    @NotNull
    private Map<String, EncryptedTwoFactorDeviceKeyDTO> encryptedTwoFactorKeys;

    public EncryptedTwoFactorKey toEncryptedTwoFactorKey(@NonNull String ownerId) {
        return new EncryptedTwoFactorKey(
                getUserId(),
                ownerId,
                encryptedTwoFactorKeys.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey(),
                        entry -> entry.getValue().toEncryptedTwoFactorDeviceKey()))
        );
    }
}
