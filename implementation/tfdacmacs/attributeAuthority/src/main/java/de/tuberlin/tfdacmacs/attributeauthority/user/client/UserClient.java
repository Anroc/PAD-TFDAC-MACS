package de.tuberlin.tfdacmacs.attributeauthority.user.client;

import de.tuberlin.tfdacmacs.attributeauthority.certificate.client.CertificateClient;
import de.tuberlin.tfdacmacs.attributeauthority.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.attributeauthority.client.CAClient;
import de.tuberlin.tfdacmacs.attributeauthority.config.AttributeAuthorityConfig;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
import de.tuberlin.tfdacmacs.lib.user.data.DeviceState;
import de.tuberlin.tfdacmacs.lib.user.data.dto.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserClient {

    private final CAClient caClient;
    private final CertificateClient certificateClient;
    private final AttributeAuthorityConfig config;

    public void createUserForCA(@NonNull User user) {
        log.info("Requesting user creation on CA site.");
        caClient.createUser(new UserCreationRequest(user.getId(), config.getId()));
    }

    public User extendWithCAUser(@NonNull User user) {
        log.info("Requesting user from CA.");
        UserResponse userResponse = caClient.getUser(user.getId());

        getMissingCertificateIds(user, userResponse)
            .stream()
            .map(id -> certificateClient.getCertificate(id, user.getId()))
                .forEach(certificate -> {

            if( getDevice(userResponse, certificate.getId()).getDeviceState() == DeviceState.ACTIVE) {
                        user.getDevices().add(certificate);
                    } else {
                        user.getUnapprovedDevices().add(certificate);
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

    private DeviceResponse getDevice(UserResponse userResponse, String deviceId) {
        return userResponse.getDevices().stream()
                .filter(device -> device.getCertificateId().equals(deviceId))
                .findAny()
                .get();
    }

    private Set<String> getMissingCertificateIds(@NonNull User user, @NonNull UserResponse userResponse) {
        Set<String> knownCertIds = user.getDevices().stream().map(Certificate::getId).collect(Collectors.toSet());
        knownCertIds.addAll(user.getUnapprovedDevices().stream().map(Certificate::getId).collect(Collectors.toSet()));

        Set<String> newCertIds = userResponse.getDevices().stream().map(DeviceResponse::getCertificateId)
                .collect(Collectors.toSet());
        newCertIds.removeAll(knownCertIds);
        return newCertIds;
    }
}
