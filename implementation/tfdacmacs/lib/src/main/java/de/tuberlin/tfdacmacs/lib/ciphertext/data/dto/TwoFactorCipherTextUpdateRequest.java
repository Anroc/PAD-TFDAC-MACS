package de.tuberlin.tfdacmacs.lib.ciphertext.data.dto;

import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.VersionedID;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.CipherText2FAUpdateKey;
import it.unisa.dia.gas.jpbc.Field;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TwoFactorCipherTextUpdateRequest {

    @NotBlank
    private String ownerId;

    @Min(0)
    private long targetVersion;

    @NotNull
    @NotEmpty
    private List<TwoFactorCipherTextUpdateKey> updates;

    public Set<CipherText2FAUpdateKey> toCipherText2FAUpdateKey(Field g1) {
        return getUpdates().stream()
                .map(dto -> new CipherText2FAUpdateKey(
                        ElementConverter.convert(dto.getUpdateKey(), g1),
                        new VersionedID(dto.getAttributeValueId(), dto.getAttributeVersion()),
                        getOwnerId(),
                        targetVersion
                )).collect(Collectors.toSet());
    }
}
