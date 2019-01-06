package de.tuberlin.tfdacmacs.centralserver.certificate.factory;

import de.tuberlin.tfdacmacs.crypto.rsa.certificate.CertificateUtils;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import de.tuberlin.tfdacmacs.crypto.rsa.factory.CertificateRequestFactory;
import de.tuberlin.tfdacmacs.lib.certificate.data.dto.CertificateRequest;
import de.tuberlin.tfdacmacs.lib.certificate.util.SpringContextAwareCertificateUtils;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.openssl.jcajce.JcaMiscPEMGenerator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemWriter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;

@Component
@RequiredArgsConstructor
public class CertificateRequestTestFactory {

    private final CertificateRequestFactory certificateRequestFactory;
    private final SpringContextAwareCertificateUtils certificateUtils;

    public CertificateRequest create(String id, KeyPair keyPair) throws IOException, OperatorCreationException {
        PKCS10CertificationRequest request = certificateRequestFactory.create(id, keyPair);
        System.out.println(certificateUtils.pemFormat(request));

        return new CertificateRequest(
                KeyConverter.from(request.getEncoded()).toBase64()
        );
    }
}
