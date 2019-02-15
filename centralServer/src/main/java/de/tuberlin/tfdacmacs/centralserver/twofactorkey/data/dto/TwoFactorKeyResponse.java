package de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.dto;

import de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.EncryptedTwoFactorKey;
import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorUpdateKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private Map<String, EncryptedTwoFactorDeviceKeyDTO> encryptedTwoFactorKeys;
    @NotNull
    private List<String> updates = new ArrayList<>();

    public static TwoFactorKeyResponse from(EncryptedTwoFactorKey encryptedTwoFactorKey) {
        return new TwoFactorKeyResponse(
                encryptedTwoFactorKey.getId(),
                encryptedTwoFactorKey.getUserId(),
                encryptedTwoFactorKey.getDataOwnerId(),
                encryptedTwoFactorKey.getEncryptedTwoFactorKeys().entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                entry -> entry.getKey(),
                                entry -> EncryptedTwoFactorDeviceKeyDTO.from(entry.getValue()))),
                encryptedTwoFactorKey.getTwoFactorUpdateKeys()
                        .stream()
                        .map(TwoFactorUpdateKey::getUpdateKey)
                        .map(ElementConverter::convert)
                        .collect(Collectors.toList())
        );
    }
}
