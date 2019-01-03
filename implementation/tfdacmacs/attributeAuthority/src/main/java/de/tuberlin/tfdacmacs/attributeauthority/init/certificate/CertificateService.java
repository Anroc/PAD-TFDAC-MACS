package de.tuberlin.tfdacmacs.attributeauthority.init.certificate;

import de.tuberlin.tfdacmacs.attributeauthority.certificate.client.CertificateClient;
import de.tuberlin.tfdacmacs.attributeauthority.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.attributeauthority.config.AttributeAuthorityConfig;
import de.tuberlin.tfdacmacs.attributeauthority.init.certificate.events.RootCertificateRetrieved;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateClient certificateClient;
    private final ApplicationEventPublisher eventPublisher;
    private final AttributeAuthorityConfig config;

    private Certificate rootCertificate;

    @EventListener(ApplicationReadyEvent.class)
    public void initGPP() {
        if(config.isRequestCaOnInit()) {
            retrieveRootCertificate();
        }
    }

    private Certificate retrieveRootCertificate() {
        this.rootCertificate = certificateClient.getRootCertificate();
        eventPublisher.publishEvent(new RootCertificateRetrieved(this.rootCertificate));
        return rootCertificate;
    }

    public Certificate getRootCertificate() {
        if (rootCertificate == null) {
            this.rootCertificate = retrieveRootCertificate();
        }
        return rootCertificate;
    }
}
