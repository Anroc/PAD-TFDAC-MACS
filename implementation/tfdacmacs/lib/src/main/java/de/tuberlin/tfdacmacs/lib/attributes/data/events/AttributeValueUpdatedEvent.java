package de.tuberlin.tfdacmacs.lib.attributes.data.events;

import de.tuberlin.tfdacmacs.lib.attributes.data.Attribute;
import de.tuberlin.tfdacmacs.lib.attributes.data.AttributeValue;
import de.tuberlin.tfdacmacs.lib.events.DomainEvent;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class AttributeValueUpdatedEvent extends DomainEvent<Attribute> {

    private final AttributeValue revokedAttributeValue;
    private final AttributeValue newAttributeValue;

    public AttributeValueUpdatedEvent(@NonNull Attribute attribute, @NonNull AttributeValue attributeValue, @NonNull AttributeValue newAttributeValue) {
        super(attribute);
        this.revokedAttributeValue = attributeValue;
        this.newAttributeValue = newAttributeValue;
    }
}
