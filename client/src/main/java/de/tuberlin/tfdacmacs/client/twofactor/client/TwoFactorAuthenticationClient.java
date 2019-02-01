package de.tuberlin.tfdacmacs.client.twofactor.client;

import de.tuberlin.tfdacmacs.client.attribute.client.dto.DeviceResponse;
import de.tuberlin.tfdacmacs.client.authority.exception.CertificateManipulationException;
import de.tuberlin.tfdacmacs.client.authority.exception.NotTrustedAuthorityException;
import de.tuberlin.tfdacmacs.client.certificate.client.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.client.register.Session;
import de.tuberlin.tfdacmacs.client.rest.AAClient;
import de.tuberlin.tfdacmacs.client.rest.CAClient;
import de.tuberlin.tfdacmacs.client.rest.SemanticValidator;
import de.tuberlin.tfdacmacs.client.twofactor.client.dto.DeviceIdResponse;
import de.tuberlin.tfdacmacs.client.twofactor.client.dto.UserResponse;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorKey;
import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.certificate.CertificateUtils;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

@Slf4j
@Component
@RequiredArgsConstructor
public class TwoFactorAuthenticationClient {

    private final CAClient caClient;
    private final ApplicationContext context;
    private final SemanticValidator semanticValidator;
    private final CertificateUtils certificateUtils;
    private final StringAsymmetricCryptEngine cryptEngine;
    private final Session session;

    public void uploadTwoFactorKey(@NonNull TwoFactorKey.Public twoFactoryKey) {
        String userId = twoFactoryKey.getUserId();

        UserResponse user = caClient.getUser(userId);
        String aid = user.getAuthorityId();

        if(! semanticValidator.isTrustedAuthority(aid)) {
            throw new NotTrustedAuthorityException(
                    String.format("User with id [%s] of authority [%s] is untrusted.",
                    userId, aid));
        }

        AAClient aaClient = getAAClient(aid);
        user.getDevices().stream()
                .map(DeviceResponse::getCertificateId)
                .forEach(deviceId -> aaClient.getDevice(userId, deviceId)
                            .map(DeviceIdResponse::getId)
                            .map(this::getCertificate)
                            .map(X509Certificate::getPublicKey)
                            .map(publicKey -> encrypt(twoFactoryKey.getKey(), publicKey))
                            .ifPresent(encryptedTwoFactorKey -> caClient.createTwoFactorKey(userId, deviceId, encryptedTwoFactorKey))
                );
    }

    private X509Certificate getCertificate(String certificateId) {
        CertificateResponse certificateResponse = caClient.getCertificate(certificateId);
        X509Certificate certificate = KeyConverter.from(certificateResponse.getCertificate()).toX509Certificate();
        String fingerprint = certificateUtils.fingerprint(certificate);
        if(! fingerprint.equals(certificateId)) {
            throw new CertificateManipulationException(certificateId, fingerprint);
        }

        return certificate;
    }

    private AAClient getAAClient(String aid) {
        return context.getBean(aid, AAClient.class);
    }

    private String encrypt(Element element, PublicKey publicKey) {
        try {
            return cryptEngine.encryptRaw(element.toBytes(), publicKey);
        } catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }

}
