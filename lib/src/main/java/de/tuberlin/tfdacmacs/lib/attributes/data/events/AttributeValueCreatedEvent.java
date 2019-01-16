package de.tuberlin.tfdacmacs.lib.attributes.data.events;

import de.tuberlin.tfdacmacs.lib.attributes.data.AbstractAttribute;
import de.tuberlin.tfdacmacs.lib.attributes.data.AttributeValueComponent;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AttributeValueCreatedEvent extends AttributeEvent {

    private final AttributeValueComponent value;

    public AttributeValueCreatedEvent(AbstractAttribute source, AttributeValueComponent value) {
        super(source);
        this.value = value;
    }
}
