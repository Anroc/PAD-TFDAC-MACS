package de.tuberlin.tfdacmacs.client.attribute.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.tuberlin.tfdacmacs.crypto.pairing.data.VersionedID;
import it.unisa.dia.gas.jpbc.Element;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attribute {

    @NotBlank
    private String id;
    @Min(0)
    private long version;
    @NotNull
    private Element key;

    @JsonIgnore
    public VersionedID asVersionedID() {
        return new VersionedID(id, version);
    }

}
