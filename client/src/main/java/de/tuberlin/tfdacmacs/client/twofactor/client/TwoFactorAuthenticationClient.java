package de.tuberlin.tfdacmacs.client.twofactor.client;

import de.tuberlin.tfdacmacs.client.attribute.client.dto.DeviceResponse;
import de.tuberlin.tfdacmacs.client.authority.exception.CertificateManipulationException;
import de.tuberlin.tfdacmacs.client.authority.exception.NotTrustedAuthorityException;
import de.tuberlin.tfdacmacs.client.certificate.client.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.client.gpp.GPPService;
import de.tuberlin.tfdacmacs.client.rest.AAClient;
import de.tuberlin.tfdacmacs.client.rest.CAClient;
import de.tuberlin.tfdacmacs.client.rest.SemanticValidator;
import de.tuberlin.tfdacmacs.client.rest.session.Session;
import de.tuberlin.tfdacmacs.client.twofactor.client.dto.*;
import de.tuberlin.tfdacmacs.client.twofactor.data.PublicTwoFactorAuthentication;
import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorKey;
import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.StringSymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.SymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.certificate.CertificateUtils;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TwoFactorAuthenticationClient {

    private final CAClient caClient;
    private final ApplicationContext context;
    private final Session session;
    private final SemanticValidator semanticValidator;
    private final CertificateUtils certificateUtils;
    private final StringAsymmetricCryptEngine asymmetricCryptEngine;
    private final GPPService gppService;

    public void uploadTwoFactorKey(@NonNull TwoFactorKey.Public twoFactoryKey) {
        String userId = twoFactoryKey.getUserId();

        UserResponse user = caClient.getUser(userId);
        String aid = user.getAuthorityId();

        if (!semanticValidator.isTrustedAuthority(aid)) {
            throw new NotTrustedAuthorityException(
                    String.format("User with id [%s] of authority [%s] is untrusted.",
                            userId, aid));
        }

        AAClient aaClient = getAAClient(aid);

        Map<String, EncryptedTwoFactorDeviceKeyDTO> encryptedTwoFactorKeys = new HashMap<>();
        StringSymmetricCryptEngine symmetricCryptEngine = new StringSymmetricCryptEngine();

        user.getDevices().stream()
                .map(DeviceResponse::getCertificateId)
                .forEach(deviceId -> aaClient.getDevice(userId, deviceId)
                        .map(DeviceIdResponse::getId)
                        .map(this::getCertificate)
                        .map(X509Certificate::getPublicKey)
                        .map(publicKey -> new EncryptedTwoFactorDeviceKeyDTO(
                                encryptAsymmetrically(publicKey, symmetricCryptEngine.getSymmetricCipherKey()),
                                encryptSymmetrically(twoFactoryKey.getKey(), symmetricCryptEngine)))
                        .ifPresent(encryptedTwoFactorKey -> encryptedTwoFactorKeys.put(deviceId, encryptedTwoFactorKey))
                );

        TwoFactorKeyRequest twoFactorKeyRequest = new TwoFactorKeyRequest(
                userId,
                encryptedTwoFactorKeys
        );

        log.info("Uploading 2FA key for user {} and devices {}", userId, encryptedTwoFactorKeys.keySet());
        caClient.createTwoFactorKey(twoFactorKeyRequest);
    }

    private X509Certificate getCertificate(String certificateId) {
        CertificateResponse certificateResponse = caClient.getCertificate(certificateId);
        X509Certificate certificate = KeyConverter.from(certificateResponse.getCertificate()).toX509Certificate();
        String fingerprint = certificateUtils.fingerprint(certificate);
        if (!fingerprint.equals(certificateId)) {
            throw new CertificateManipulationException(certificateId, fingerprint);
        }

        return certificate;
    }

    public AAClient getAAClient(String aid) {
        return context.getBean(aid, AAClient.class);
    }

    private String encryptSymmetrically(Element element, SymmetricCryptEngine symmetricCryptEngine) {
        try {
            return Base64.encodeBase64String(symmetricCryptEngine.encryptRaw(
                    element.toBytes(),
                    symmetricCryptEngine.getSymmetricCipherKey()));
        } catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }

    private Element decryptSymmetrically(String cipher, SymmetricCryptEngine symmetricCryptEngine) {
        try {
            return ElementConverter.convert(
                    symmetricCryptEngine.decryptRaw(
                            Base64.decodeBase64(cipher),
                            symmetricCryptEngine.getSymmetricCipherKey()),
                    gppService.getGPP().getPairing().getG1()
            );
        } catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }

    private String encryptAsymmetrically(PublicKey publicKey, Key symmetricKey) {
        try {
            return asymmetricCryptEngine.encryptRaw(symmetricKey.getEncoded(), publicKey);
        } catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }

    private StringSymmetricCryptEngine decryptAsymmetrically(String encryptedKey, PrivateKey privateKey) {
        try {
            StringSymmetricCryptEngine cryptEngine = new StringSymmetricCryptEngine();
            byte[] bytes = asymmetricCryptEngine.decryptRaw(encryptedKey, privateKey);
            cryptEngine.setSymmetricCipherKey(cryptEngine.createKeyFromBytes(bytes));
            return cryptEngine;
        } catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }

    public List<PublicTwoFactorAuthentication> updateTwoFactorKeys() {
        List<TwoFactorKeyResponse> twoFactorKeys = caClient.getTwoFactorKeys(session.getEmail());
        return twoFactorKeys.stream()
                .filter(twoFactorKeyResponse -> twoFactorKeyResponse.getUserId().equals(session.getEmail()))
                .map(twoFactorKeyResponse -> {
                    Map<String, EncryptedTwoFactorDeviceKeyDTO> encryptedTwoFactorKeys =
                            twoFactorKeyResponse.getEncryptedTwoFactorKeys();
                    return Optional.ofNullable(encryptedTwoFactorKeys.get(session.getCertificate().getId()))
                            .map(encryptedTwoFactorDeviceKeyDTO -> {
                                StringSymmetricCryptEngine cryptEngine = decryptAsymmetrically(
                                        encryptedTwoFactorDeviceKeyDTO.getEncryptedSymmetricKey(),
                                        session.getKeyPair().getPrivateKey()
                                );

                                return new PublicTwoFactorAuthentication(
                                        twoFactorKeyResponse.getOwnerId(),
                                        twoFactorKeyResponse.getUserId(),
                                        new TwoFactorKey.Public(
                                                session.getEmail(),
                                                decryptSymmetrically(encryptedTwoFactorDeviceKeyDTO.getEncryptedKey(),
                                                        cryptEngine)
                                        )
                                );
                            });
                }).filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
