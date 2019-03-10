package de.tuberlin.tfdacmacs.attributeauthority.user.client;

import de.tuberlin.tfdacmacs.attributeauthority.certificate.client.CertificateClient;
import de.tuberlin.tfdacmacs.attributeauthority.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.attributeauthority.client.CAClient;
import de.tuberlin.tfdacmacs.attributeauthority.client.ContentSigner;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.VersionedID;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.UserAttributeValueUpdateKey;
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
    private final ContentSigner contentSigner;

    public void createUserForCA(@NonNull User user) {
        log.info("Requesting user creation on CA site.");
        caClient.createUser(new UserCreationRequest(user.getId()));
    }

    public User extendWithCAUser(@NonNull User user) {
        log.info("Requesting user from CA.");
        UserResponse userResponse = caClient.getUser(user.getId());

        getMissingCertificateIds(user, userResponse)
                .stream()
                .map(id -> certificateClient.getCertificate(id, user.getId()))
                // we can only relay on our data base for active devices. So that the CA can not just
                // add a active device in its database.
                // for that reason we add each unknow device directly into unapproved devices
                .forEach(certificate -> user.getUnapprovedDevices().add(certificate));
        return user;
    }

    public void updateDeviceForEncryptedAttributeValueKeys(@NonNull String userId, @NonNull String deviceId, @NonNull String encryptedKey, @NonNull Map<VersionedID, String> encryptedAttributeValueKeys) {
        Set<EncryptedAttributeValueKeyDTO> encryptedAttributeValueKeyDTOs = encryptedAttributeValueKeys.entrySet()
                .stream()
                .map(entry -> new EncryptedAttributeValueKeyDTO(entry.getKey().getId(), entry.getKey().getVersion(), entry.getValue()))
                .collect(Collectors.toSet());

        caClient.updateDevice(
                userId,
                deviceId,
                new DeviceUpdateRequest(DeviceState.ACTIVE, encryptedKey, encryptedAttributeValueKeyDTOs)
        );
    }

    private Set<String> getMissingCertificateIds(@NonNull User user, @NonNull UserResponse userResponse) {
        Set<String> knownCertIds = user.getDevices().stream().map(Certificate::getId).collect(Collectors.toSet());
        knownCertIds.addAll(user.getUnapprovedDevices().stream().map(Certificate::getId).collect(Collectors.toSet()));

        Set<String> newCertIds = userResponse.getDevices().stream().map(DeviceResponse::getCertificateId)
                .collect(Collectors.toSet());
        newCertIds.removeAll(knownCertIds);
        return newCertIds;
    }

    public void updateUserSecretKey(
            @NonNull UserAttributeValueUpdateKey updateKey,
            @NonNull String attributeValueId,
            long targetVersion,
            long newVersion) {
        String userId = updateKey.getUserId();

        AttributeValueUpdateKeyDTO attributeValueUpdateKeyDTO = new AttributeValueUpdateKeyDTO(
                ElementConverter.convert(updateKey.getUpdateKey()),
                attributeValueId,
                targetVersion,
                newVersion,
                null
        );

        attributeValueUpdateKeyDTO.setSignature(
                attributeValueUpdateKeyDTO.signature()
                    .pack(attributeValueUpdateKeyDTO.buildSignatureBody())
                    .pack(userId)
                    .finalize(contentSigner::sign)
        );

        caClient.updateAttributeValueUpdateKey(userId, attributeValueUpdateKeyDTO);
    }
}
