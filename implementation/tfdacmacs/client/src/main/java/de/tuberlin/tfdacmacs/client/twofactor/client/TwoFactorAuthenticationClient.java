package de.tuberlin.tfdacmacs.client.twofactor.client;

import de.tuberlin.tfdacmacs.client.user.client.dto.DeviceResponse;
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
import de.tuberlin.tfdacmacs.client.user.client.dto.TwoFactorPublicKeyDTO;
import de.tuberlin.tfdacmacs.client.user.client.dto.UserResponse;
import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AsymmetricElementKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorUpdateKey;
import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.StringSymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.SymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.certificate.CertificateUtils;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
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
import java.util.*;
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

    public String uploadTwoFactorKey(@NonNull TwoFactorKey.Public twoFactoryKey) {
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
        TwoFactorKeyResponse twoFactorKey = caClient.createTwoFactorKey(twoFactorKeyRequest);
        return twoFactorKey.getId();
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

    public AAClient getAAClient(@NonNull String aid) {
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
                    getG1()
            );
        } catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }

    private Field getG1() {
        return gppService.getGPP().getPairing().getG1();
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

    public List<PublicTwoFactorAuthentication> getTwoFactorKeys() {
        List<TwoFactorKeyResponse> twoFactorKeys = caClient.getTwoFactorKeys();
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

                                TwoFactorKey.Public tfPublic = new TwoFactorKey.Public(
                                        session.getEmail(),
                                        decryptSymmetrically(encryptedTwoFactorDeviceKeyDTO.getEncryptedKey(),
                                                cryptEngine)
                                );

                                // apply all updates
                                twoFactorKeyResponse.getUpdates().forEach(updateBase64Key ->
                                        tfPublic.update(new TwoFactorUpdateKey(
                                                twoFactorKeyResponse.getUserId(),
                                                ElementConverter.convert(updateBase64Key, getG1())))
                                );

                                return new PublicTwoFactorAuthentication(
                                        twoFactorKeyResponse.getOwnerId(),
                                        twoFactorKeyResponse.getUserId(),
                                        tfPublic

                                );
                            });
                }).filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public void updateTwoFactorKeys(@NonNull List<TwoFactorUpdateKey> user2FAUpdateKeys) {
        List<TwoFactorKeyResponse> twoFactorKeys = getTwoFactorKeyResponsesByOwnerId();
        user2FAUpdateKeys.stream()
                .forEach(user2FAUpdateKey -> {
                    String twoFactorId = getIdForUserId(user2FAUpdateKey.getUserId(), twoFactorKeys);
                    caClient.putTwoFactorUpdateKey(
                            twoFactorId,
                            new TwoFactorUpdateKeyRequest(
                                    ElementConverter.convert(user2FAUpdateKey.getUpdateKey())
                            ));
                });
    }

    public void deleteTwoFactorKeys(@NonNull Set<String> revokedUserIds) {
        List<TwoFactorKeyResponse> twoFactorKeys = getTwoFactorKeyResponsesByOwnerId();
        revokedUserIds.stream()
                .forEach(revokedUserId -> {
                    String twoFactorId = getIdForUserId(revokedUserId, twoFactorKeys);
                    caClient.deleteTwoFactorKey(twoFactorId);
                });
    }

    private List<TwoFactorKeyResponse> getTwoFactorKeyResponsesByOwnerId() {
        return caClient.getTwoFactorKeys()
                .stream()
                .filter(response -> response.getOwnerId().equals(session.getEmail()))
                .collect(Collectors.toList());
    }

    private String getIdForUserId(String userId, List<TwoFactorKeyResponse> twoFactorKeyResponses) {
        return twoFactorKeyResponses.stream().filter(twoFactorKeyResponse -> twoFactorKeyResponse.getUserId().equals(userId))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Could not find 2FA response object for user " + userId))
                .getId();
    }

    public void updateUserForTwoFactorPublicKey(@NonNull AsymmetricElementKey.Public twoFactorPublicKey) {
        String encodedPublicKey = ElementConverter.convert(twoFactorPublicKey.getKey());

        try {
            String signature = asymmetricCryptEngine.sign(
                    session.getEmail() + ";" + encodedPublicKey,
                    session.getKeyPair().getPrivateKey()
            );

            TwoFactorPublicKeyDTO twoFactorPublicKeyDTO = new TwoFactorPublicKeyDTO(
                    encodedPublicKey,
                    signature
            );

            caClient.updateTwoFactorPublicKey(session.getEmail(), twoFactorPublicKeyDTO);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
