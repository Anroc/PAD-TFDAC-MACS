package de.tuberlin.tfdacmacs.attributeauthority.init.certificate.events;

import de.tuberlin.tfdacmacs.attributeauthority.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.lib.events.DomainEvent;

public class RootCertificateRetrieved extends DomainEvent<Certificate> {
    public RootCertificateRetrieved(Certificate source) {
        super(source);
    }
}
