package de.tuberlin.tfdacmacs.centralserver.certificate.utils;

import de.tuberlin.tfdacmacs.centralserver.certificate.config.CertificateConfig;
import de.tuberlin.tfdacmacs.crypto.rsa.certificate.CertificateSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CertificateRequestProcessor extends CertificateSigner {

    @Autowired
    public CertificateRequestProcessor(CertificateConfig certificateConfig) {
        super(certificateConfig.getDomains(), certificateConfig.getIps(), certificateConfig.getValidForDays());
    }
}
