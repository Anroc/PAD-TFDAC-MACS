package de.tuberlin.tfdacmacs.basics.attributes.data;

import de.tuberlin.tfdacmacs.basics.db.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
public class Attribute<T> extends Entity {

    @NotBlank
    private String authorityDomain;
    @NotBlank
    private String name;
    @NotBlank
    private List<AttributeValue<T>> values;
    @NotNull
    private AttributeType type;

    public Attribute(String authorityDomain,
            String name,
            List<AttributeValue<T>> values,
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
