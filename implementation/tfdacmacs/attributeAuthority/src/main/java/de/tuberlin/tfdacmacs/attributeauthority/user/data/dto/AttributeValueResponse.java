package de.tuberlin.tfdacmacs.attributeauthority.user.data.dto;

import de.tuberlin.tfdacmacs.attributeauthority.user.data.UserAttributeKey;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.converter.ElementConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeValueResponse {

    @NotBlank
    private String attributeId;
    @NotNull
    private Object value;
    @NotBlank
    private String key;

    public static AttributeValueResponse from(@NonNull UserAttributeKey userAttributeKey) {
        return new AttributeValueResponse(
                userAttributeKey.getAttributeId(),
                userAttributeKey.getValue(),
                ElementConverter.convert(userAttributeKey.getKey().getSecretKey())
        );
    }
}
