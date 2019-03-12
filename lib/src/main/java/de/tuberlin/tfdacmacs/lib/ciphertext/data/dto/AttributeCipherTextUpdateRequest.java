package de.tuberlin.tfdacmacs.lib.ciphertext.data.dto;

import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.CipherTextAttributeUpdateKey;
import de.tuberlin.tfdacmacs.crypto.pairing.exceptions.VersionMismatchException;
import it.unisa.dia.gas.jpbc.Field;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttributeCipherTextUpdateRequest {

    @NotBlank
    private String attributeValueId;

    @Min(0)
    private long targetVersion;

    @Valid
    @NotEmpty
    private Map<String, AttributeCipherTextUpdateKeyDTO> cipherTextUpdateKeys;

    public Map<String, CipherTextAttributeUpdateKey> toCipherTextUpdateKeys(
            @NonNull Field g1,
            @NonNull AttributeValueKey.Public newAttributeValuePublicKey) {

        if(newAttributeValuePublicKey.getVersion() - 1 != getTargetVersion()) {
            throw new VersionMismatchException(
                    String.format("Given attribute value public key is in version %d, but expected version %d.",
                            newAttributeValuePublicKey.getVersion(), targetVersion + 1));
        }

        return cipherTextUpdateKeys.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey(),
                        entry -> new CipherTextAttributeUpdateKey(
                                ElementConverter.convert(entry.getValue().getUpdateKey(), g1),
                                getAttributeValueId(),
                                entry.getValue().getDataOwnerId(),
                                newAttributeValuePublicKey,
                                getTargetVersion()
                        )));
    }
}
