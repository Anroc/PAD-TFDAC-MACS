package de.tuberlin.tfdacmacs.attributeauthority.certificate;

import de.tuberlin.tfdacmacs.attributeauthority.certificate.data.Certificate;

public interface RootCertificateProvider {

    Certificate getRootCertificate();
}
