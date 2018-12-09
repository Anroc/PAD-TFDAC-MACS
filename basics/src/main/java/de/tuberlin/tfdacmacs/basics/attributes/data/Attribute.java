package de.tuberlin.tfdacmacs.basics.attributes.data;

import de.tuberlin.tfdacmacs.basics.db.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Attribute extends Entity {

    @NotBlank
    private String authorityDomain;
    @NotBlank
    private String name;
    @NotEmpty
    private List<AttributeValue> values;
    @NotNull
    private AttributeType type;

    public Attribute(String authorityDomain,
            String name,
            List<AttributeValue> values,
            AttributeType type) {
        super(generateId(authorityDomain, name));
        this.authorityDomain = authorityDomain;
        this.name = name;
        this.values = values;
        this.type = type;
    }

    public static String generateId(String authorityDomain, String name) {
        return authorityDomain + "." + name;
    }
}
