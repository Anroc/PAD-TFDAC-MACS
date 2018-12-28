package de.tuberlin.tfdacmacs.basics.gpp.events;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.basics.events.DomainEvent;
import lombok.NonNull;

public class GlobalPublicParameterChangedEvent extends DomainEvent<GlobalPublicParameter> {
    public GlobalPublicParameterChangedEvent(@NonNull GlobalPublicParameter source) {
        super(source);
    }
}
