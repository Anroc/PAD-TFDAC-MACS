package de.tuberlin.tfdacmacs.client.register;

import de.tuberlin.tfdacmacs.client.keypair.KeyPairFactory;
import de.tuberlin.tfdacmacs.client.register.data.dto.CertificateRequest;
import de.tuberlin.tfdacmacs.client.register.data.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.client.rest.CaClient;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import de.tuberlin.tfdacmacs.crypto.rsa.factory.CertificateRequestFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final CaClient caClient;
    private final CertificateRequestFactory certificateRequestFactory;
    private final KeyPairFactory keyPairFactory;

    public void certificateRequest(@NonNull String email) {
        try {
            PKCS10CertificationRequest pkcs10CertificationRequest = certificateRequestFactory
                    .create(email, keyPairFactory.getKeyPair());
            CertificateRequest certificateRequest = new CertificateRequest(
                    KeyConverter.from(pkcs10CertificationRequest.getEncoded()).toBase64()
            );

            CertificateResponse certificateResponse = caClient.certificateRequest(certificateRequest);
            log.info("received certificate with id [{}] for user [{}]", certificateResponse.getId(), email);
        } catch (OperatorCreationException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
