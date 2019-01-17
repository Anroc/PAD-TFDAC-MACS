package de.tuberlin.tfdacmacs.lib.attributes.data;

import de.tuberlin.tfdacmacs.lib.attributes.data.events.AttributeCreatedEvent;
import de.tuberlin.tfdacmacs.lib.attributes.data.events.AttributeValueCreatedEvent;
import de.tuberlin.tfdacmacs.lib.db.Entity;
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
public abstract class AbstractAttribute<T extends AttributeValueComponent> extends Entity {

    @NotBlank
    private String authorityDomain;
    @NotBlank
    private String name;
    @NotEmpty
    private Set<T> values;
    @NotNull
    private AttributeType type;

    protected AbstractAttribute(String authorityDomain,
            String name,
            Set<T> values,
            AttributeType type) {
        super(generateId(authorityDomain, name));
        this.authorityDomain = authorityDomain;
        this.name = name;
        this.type = type;
        addValues(values);
    }

    public static Attribute createAttribute(@NonNull String authorityDomain, @NonNull String name, @NonNull Set<AttributeValue> values, @NonNull AttributeType type) {
        Attribute attribute = new Attribute(
                authorityDomain,
                name,
                values,
                type
        );

        attribute.registerDomainEvent(new AttributeCreatedEvent(attribute));
        return attribute;
    }

    public static PublicAttribute createPublicAttribute(@NonNull String authorityDomain, @NonNull String name, @NonNull Set<PublicAttributeValue> values, @NonNull AttributeType type) {
        PublicAttribute attribute = new PublicAttribute(
                authorityDomain,
                name,
                values,
                type
        );
        return attribute;
    }

    private final AbstractAttribute addValues(@NonNull Set<T> values) {
        values.forEach(this::doAddValue);
        return this;
    }

    private final AbstractAttribute doAddValue(@NonNull T attributeValue) {
        typeCheck(attributeValue);
        if(values == null) {
            this.values = new HashSet<>();
        }

        this.values.add(attributeValue);
        return this;
    }

    public AbstractAttribute addValue(@NonNull T attributeValue) {
        doAddValue(attributeValue);
        registerDomainEvent(new AttributeValueCreatedEvent(this, attributeValue));
        return this;
    }

    public void typeCheck(@NonNull T attributeValue) {
        if (! type.matchesType(attributeValue.getValue())) {
            throw new IllegalArgumentException(
                    String.format("The value of %s did not match the type of this attribute: %s.",
                            attributeValue, getType()));
        }
    }

    public static String generateId(String authorityDomain, String name) {
        return authorityDomain + "." + name;
    }
}
