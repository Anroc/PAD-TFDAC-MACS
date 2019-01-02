package de.tuberlin.tfdacmacs.attributeauthority.user.client;

import de.tuberlin.tfdacmacs.attributeauthority.certificate.exceptions.CertificateUntrustedException;
import de.tuberlin.tfdacmacs.attributeauthority.client.CAClient;
import de.tuberlin.tfdacmacs.attributeauthority.config.AttributeAuthorityConfig;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
import de.tuberlin.tfdacmacs.crypto.rsa.certificate.CertificateUtils;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import de.tuberlin.tfdacmacs.lib.user.data.DeviceState;
import de.tuberlin.tfdacmacs.lib.user.data.dto.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserClient {

    private final CAClient caClient;
    private final AttributeAuthorityConfig config;
    private final CertificateUtils certificateUtils;

    public void createUserForCA(@NonNull User user) {
        log.info("Requesting user creation on CA site.");
        caClient.createUser(new UserCreationRequest(user.getId(), config.getId()));
    }

    public User extendWithCAUser(@NonNull User user) {
        log.info("Requesting user from CA.");
        UserResponse userResponse = caClient.getUser(user.getId());

        getMissingCertificateIds(user, userResponse)
            .stream()
            .map(caClient::getCertificate)
            .collect(Collectors.toMap(
                    certResponse -> certResponse.getId(),
                    certResponse -> KeyConverter.from(certResponse.getCertificate()).toX509Certificate()
            )).forEach((certId, certificate) -> {
            validateCommonName(user, userResponse, certificate);
            validateCertificateChain(certificate);

            if( getDevice(userResponse, certId).getDeviceState() == DeviceState.ACTIVE) {
                        user.getDevices().put(certId, certificate);
                    } else {
                        user.getUnapprovedDevices().put(certId, certificate);
                }
            });

        return user;
    }

    public void updateDeviceForEncryptedAttributeValueKeys(@NonNull String userId, @NonNull String deviceId, @NonNull Map<String, String> encryptedAttributeValueKeys) {
        Set<EncryptedAttributeValueKeyDTO> encryptedAttributeValueKeyDTOs = encryptedAttributeValueKeys.entrySet()
                .stream()
                .map(entry -> new EncryptedAttributeValueKeyDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());

        caClient.updateDevice(
                userId,
                deviceId,
                new DeviceUpdateRequest(DeviceState.ACTIVE, encryptedAttributeValueKeyDTOs)
        );
    }

    private void validateCertificateChain(X509Certificate certificate) {
        // TODO: validate and fix certificate chain
        // certificateUtils.validateChain(...)
    }

    private void validateCommonName(@NonNull User user, UserResponse userResponse, X509Certificate certificate) {
        String commonName = certificateUtils.extractCommonName(certificate);
        if(! commonName.equals(user.getId()) || commonName.equals(userResponse.getId())) {
            log.error("Certificate Untrusted:\n{}", certificate.toString());
            throw new CertificateUntrustedException(
                    String.format("CommonName [%s] does not match exactly the user name in DB [%s] or "
                            + "the returned user Id in the response [%s].", commonName, user.getId(), userResponse.getId())
            );
        }
    }

    private DeviceResponse getDevice(UserResponse userResponse, String deviceId) {
        return userResponse.getDevices().stream()
                .filter(device -> device.getCertificateId().equals(deviceId))
                .findAny()
                .get();
    }

    private Set<String> getMissingCertificateIds(@NonNull User user, @NonNull UserResponse userResponse) {
        Set<String> knownCertIds = new HashSet<>(user.getDevices().keySet());
        knownCertIds.addAll(user.getUnapprovedDevices().keySet());

        Set<String> newCertIds = userResponse.getDevices().stream().map(DeviceResponse::getCertificateId)
                .collect(Collectors.toSet());
        newCertIds.removeAll(knownCertIds);
        return newCertIds;
    }
}
