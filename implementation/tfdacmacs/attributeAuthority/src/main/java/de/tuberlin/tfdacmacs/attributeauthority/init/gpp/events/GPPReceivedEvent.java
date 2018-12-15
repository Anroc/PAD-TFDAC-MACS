package de.tuberlin.tfdacmacs.attributeauthority.init.gpp.events;

import de.tuberlin.tfdacmacs.basics.events.DomainEvent;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.GlobalPublicParameter;

public class GPPReceivedEvent extends DomainEvent<GlobalPublicParameter> {
    public GPPReceivedEvent(GlobalPublicParameter source) {
        super(source);
    }
}
