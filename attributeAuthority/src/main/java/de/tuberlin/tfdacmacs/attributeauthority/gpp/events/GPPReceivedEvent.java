package de.tuberlin.tfdacmacs.attributeauthority.gpp.events;

import de.tuberlin.tfdacmacs.basics.events.DomainEvent;
import de.tuberlin.tfdacmacs.basics.gpp.data.GlobalPublicParameter;

public class GPPReceivedEvent extends DomainEvent<GlobalPublicParameter> {
    public GPPReceivedEvent(GlobalPublicParameter source) {
        super(source);
    }
}
