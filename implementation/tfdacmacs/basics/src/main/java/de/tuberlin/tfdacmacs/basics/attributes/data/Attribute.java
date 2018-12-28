package de.tuberlin.tfdacmacs.basics.attributes.data;

import com.couchbase.client.java.repository.annotation.Field;
import de.tuberlin.tfdacmacs.basics.db.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Attribute extends Entity {

    @Field
    @NotBlank
    private String authorityDomain;
    @Field
    @NotBlank
    private String name;
    @Field
    @NotEmpty
    private Set<AttributeValue> values;
    @Field
    @NotNull
    private AttributeType type;

    public Attribute(String authorityDomain,
            String name,
            Set<AttributeValue> values,
            AttributeType type) {
        super(generateId(authorityDomain, name));
        this.authorityDomain = authorityDomain;
        this.name = name;
        this.type = type;
        addValues(values);
    }

    public final Attribute addValues(@NonNull Set<AttributeValue> values) {
        values.forEach(this::addValue);
        return this;
    }

    public final Attribute addValue(@NonNull AttributeValue attributeValue) {
        if (! type.matchesType(attributeValue.getValue())) {
            throw new IllegalArgumentException(
                    String.format("The value of %s did not match the type of this attribute: %s.",
                            attributeValue, getType()));
        }
        if(values == null) {
            this.values = new HashSet<>();
        }

        this.values.add(attributeValue);
        return this;
    }

    public static String generateId(String authorityDomain, String name) {
        return authorityDomain + "." + name;
    }
}
