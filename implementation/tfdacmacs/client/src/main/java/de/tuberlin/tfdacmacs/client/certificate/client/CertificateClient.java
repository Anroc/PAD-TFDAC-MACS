package de.tuberlin.tfdacmacs.client.certificate.client;

import de.tuberlin.tfdacmacs.client.certificate.client.dto.CertificateRequest;
import de.tuberlin.tfdacmacs.client.certificate.client.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.client.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.client.certificate.db.CertificateDB;
import de.tuberlin.tfdacmacs.client.keypair.KeyPairService;
import de.tuberlin.tfdacmacs.client.rest.CAClient;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import de.tuberlin.tfdacmacs.crypto.rsa.factory.CertificateRequestFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateClient {

    private final CAClient caClient;
    private final CertificateRequestFactory certificateRequestFactory;
    private final CertificateDB certificateDB;
    private final KeyPairService keyPairService;

    public Certificate certificateRequest(@NonNull String email) {
        try {
            PKCS10CertificationRequest pkcs10CertificationRequest = certificateRequestFactory
                    .create(email, keyPairService.getKeyPair(email).toJavaKeyPair());
            CertificateRequest certificateRequest = new CertificateRequest(
                    KeyConverter.from(pkcs10CertificationRequest.getEncoded()).toBase64()
            );

            CertificateResponse certificateResponse = caClient.postCertificateRequest(certificateRequest);
            log.info("received certificate with id [{}] for user [{}]", certificateResponse.getId(), email);

            return createCertificate(email, certificateResponse);
        } catch (OperatorCreationException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Certificate createCertificate(@NonNull String email, CertificateResponse certificateResponse) {
        Certificate certificate = new Certificate(
                certificateResponse.getId(),
                email,
                KeyConverter.from(certificateResponse.getCertificate()).toX509Certificate()
        );

        certificateDB.upsert(email, certificate);
        return certificate;
    }
}
