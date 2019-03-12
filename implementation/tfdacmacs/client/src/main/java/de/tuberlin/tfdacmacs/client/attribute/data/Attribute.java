package de.tuberlin.tfdacmacs.client.attribute.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.tuberlin.tfdacmacs.crypto.pairing.data.VersionedID;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.UserAttributeValueKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attribute {

    @NotBlank
    private String id;

    @NotNull
    private UserAttributeValueKey userAttributeValueKey;

    @JsonIgnore
    public VersionedID asVersionedID() {
        return new VersionedID(id, userAttributeValueKey.getVersion());
    }

}
