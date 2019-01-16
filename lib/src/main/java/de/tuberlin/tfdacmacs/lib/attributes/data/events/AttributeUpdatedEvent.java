package de.tuberlin.tfdacmacs.lib.attributes.data.events;

import de.tuberlin.tfdacmacs.lib.attributes.data.AbstractAttribute;
import de.tuberlin.tfdacmacs.lib.attributes.data.Attribute;

public class AttributeUpdatedEvent extends AttributeEvent {
    public AttributeUpdatedEvent(AbstractAttribute source) {
        super(source);
    }
}
