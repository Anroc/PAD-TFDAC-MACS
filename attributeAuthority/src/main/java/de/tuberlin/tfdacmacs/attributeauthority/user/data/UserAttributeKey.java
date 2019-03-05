package de.tuberlin.tfdacmacs.attributeauthority.user.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.UserAttributeValueKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@EqualsAndHashCode(exclude = "key")
public class UserAttributeKey<T> {

    @NotBlank
    private String attributeId;
    @NotNull
    private T value;
    @NotNull
    private UserAttributeValueKey key;

    @JsonIgnore
    public String getAttributeValueId() {
        return String.format("%s:%s", attributeId, value.toString());
    }
}
