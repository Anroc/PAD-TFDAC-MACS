package de.tuberlin.tfdacmacs.lib.attributes.data;

import de.tuberlin.tfdacmacs.lib.attributes.data.events.AttributeValueUpdatedEvent;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Optional;
import java.util.Set;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Attribute extends AbstractAttribute<AttributeValue> {

    protected Attribute(String authorityDomain, String name,
            Set<AttributeValue> values, AttributeType type) {
        super(authorityDomain, name, values, type);
    }

    public Optional<AttributeValue> findAttributeValue(@NonNull String attributeValueId) {
        return getValues().stream()
                .filter(attributeValue -> attributeValue.getAttributeValueId().equals(attributeValueId))
                .findAny();
    }

    public Attribute updateValue(@NonNull AttributeValue newAttributeValue) {
        AttributeValue attributeValue = findAttributeValue(newAttributeValue.getAttributeValueId())
                .orElseThrow(() -> new IllegalStateException("Could not find attribute value with id: " + newAttributeValue.getAttributeValueId()));

        registerDomainEvent(new AttributeValueUpdatedEvent(this, attributeValue, newAttributeValue));
        return this;
    }
}
