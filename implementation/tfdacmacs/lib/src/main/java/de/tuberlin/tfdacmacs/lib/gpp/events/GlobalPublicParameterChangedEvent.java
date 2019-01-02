package de.tuberlin.tfdacmacs.lib.gpp.events;

import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.lib.events.DomainEvent;
import lombok.NonNull;

public class GlobalPublicParameterChangedEvent extends DomainEvent<GlobalPublicParameter> {
    public GlobalPublicParameterChangedEvent(@NonNull GlobalPublicParameter source) {
        super(source);
    }
}
