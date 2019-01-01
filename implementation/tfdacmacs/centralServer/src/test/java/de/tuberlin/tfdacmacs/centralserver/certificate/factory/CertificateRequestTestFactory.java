package de.tuberlin.tfdacmacs.centralserver.certificate.factory;

import de.tuberlin.tfdacmacs.basics.certificate.data.dto.CertificateRequest;
import de.tuberlin.tfdacmacs.basics.crypto.rsa.converter.KeyConverter;
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

    public CertificateRequest create(String id, KeyPair keyPair) throws IOException, OperatorCreationException {
        PKCS10CertificationRequest request = certificateRequestFactory.create(id, keyPair);
        printPEMFormat(request);

        return new CertificateRequest(
                KeyConverter.from(request.getEncoded()).toBase64()
        );
    }

    public void printPEMFormat(Object o) throws IOException {
        StringWriter sw = new StringWriter();
        try (PemWriter pw = new PemWriter(sw)) {
            PemObjectGenerator gen = new JcaMiscPEMGenerator(o);
            pw.writeObject(gen);
        }
        System.out.println(sw.toString());
    }
}
