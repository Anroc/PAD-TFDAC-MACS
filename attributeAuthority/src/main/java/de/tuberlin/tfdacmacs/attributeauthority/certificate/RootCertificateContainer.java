package de.tuberlin.tfdacmacs.attributeauthority.certificate;

import de.tuberlin.tfdacmacs.attributeauthority.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.attributeauthority.certificate.events.RootCertificateRetrieved;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class RootCertificateContainer implements RootCertificateProvider {

    private Certificate rootCertificate;

    @EventListener(RootCertificateRetrieved.class)
    public void updateGlobalPublicParameter(RootCertificateRetrieved event) {
        this.rootCertificate = event.getSource();
    }

    @Override
    public Certificate getRootCertificate() {
        if(rootCertificate == null) {
            throw new IllegalStateException("RootCertificate accessed before they where retrieved!");
        }
        return this.rootCertificate;
    }
}
