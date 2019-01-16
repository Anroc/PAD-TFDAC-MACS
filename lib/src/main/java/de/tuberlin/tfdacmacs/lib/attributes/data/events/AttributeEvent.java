package de.tuberlin.tfdacmacs.lib.attributes.data.events;

import de.tuberlin.tfdacmacs.lib.attributes.data.AbstractAttribute;
import de.tuberlin.tfdacmacs.lib.attributes.data.Attribute;
import de.tuberlin.tfdacmacs.lib.events.DomainEvent;

public abstract class AttributeEvent extends DomainEvent<AbstractAttribute> {
    public AttributeEvent(AbstractAttribute source) {
        super(source);
    }
}
