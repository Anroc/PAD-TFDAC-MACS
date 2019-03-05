package de.tuberlin.tfdacmacs.centralserver.user;

import de.tuberlin.tfdacmacs.centralserver.authority.AttributeAuthorityService;
import de.tuberlin.tfdacmacs.centralserver.security.AuthenticationFacade;
import de.tuberlin.tfdacmacs.centralserver.user.data.Device;
import de.tuberlin.tfdacmacs.centralserver.user.data.EncryptedAttributeValueKey;
import de.tuberlin.tfdacmacs.centralserver.user.data.TwoFactorPublicKey;
import de.tuberlin.tfdacmacs.centralserver.user.data.User;
import de.tuberlin.tfdacmacs.lib.db.exception.EntityDoesExistException;
import de.tuberlin.tfdacmacs.lib.exceptions.NotFoundException;
import de.tuberlin.tfdacmacs.lib.exceptions.ServiceException;
import de.tuberlin.tfdacmacs.lib.user.data.DeviceState;
import de.tuberlin.tfdacmacs.lib.user.data.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final AttributeAuthorityService attributeAuthorityService;
    private final AuthenticationFacade authenticationFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_AUTHORITY')")
    public UserResponse createUser(@Valid @RequestBody UserCreationRequest userCreationRequest) {
        User user = new User(userCreationRequest.getId(), authenticationFacade.getId());
        user.setAuthorityId(authenticationFacade.getId());

        if( !attributeAuthorityService.exist(authenticationFacade.getId())) {
            throw new ServiceException("Authority with id [%s] does not exist.", HttpStatus.UNPROCESSABLE_ENTITY, authenticationFacade.getId());
        }

        try{
            userService.insertUser(user);
        } catch (EntityDoesExistException e) {
            throw new ServiceException("User does already exist", e, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return toUserResponse(user);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_AUTHORITY')")
    public List<UserResponse> getUsers() {
        String authorityId = authenticationFacade.getId();
        List<User> users = userService.findUsersByAuthorityId(authorityId);
        return users.stream().map(this::toUserResponse).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_AUTHORITY', 'ROLE_USER')")
    public UserResponse getUser(@PathVariable("id") String userId) {
        User user = userService.findUser(userId)
                .orElseThrow(
                        () -> new NotFoundException(userId)
                );
        return toUserResponse(user);
    }

    @GetMapping("/{userId}/devices/{deviceId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public DeviceResponse getDevice(
            @PathVariable("userId") String userId,
            @PathVariable("deviceId") String deviceId) {
        User user = userService.findUser(userId).orElseThrow(
                () -> new NotFoundException(userId)
        );

        if(! authenticationFacade.getId().equals(user.getId())) {
            throw new ServiceException("User is not allowed to access other users except himself.", HttpStatus.FORBIDDEN);
        }

        Device device = user.findDevice(deviceId).orElseThrow(
                () -> new ServiceException("Device with id [%s] does not exist.", HttpStatus.UNPROCESSABLE_ENTITY, deviceId)
        );

        if(device.getDeviceState() != DeviceState.ACTIVE) {
            throw new ServiceException("Device is not active.", HttpStatus.PRECONDITION_FAILED)
                    .andDoNotPrintStackTrace();
        }

        return toDeviceResponse(device);
    }

    @PutMapping("/{userId}/twoFactorPublicKey")
    @PreAuthorize("hasRole('ROLE_USER')")
    public UserResponse update2FAPublicKey(
            @PathVariable("userId") String userId,
            @Valid @RequestBody TwoFactorPublicKeyDTO twoFactorPublicKeyDTO) {
        User user = userService.findUser(userId).orElseThrow(
                () -> new NotFoundException(userId)
        );

        if(! user.getId().equals(authenticationFacade.getId())) {
            throw new ServiceException("This is not you.", HttpStatus.FORBIDDEN);
        }

        user.setTwoFactorPublicKey(
                new TwoFactorPublicKey(
                        twoFactorPublicKeyDTO.getTwoFactorAuthenticationPublicKey(),
                        twoFactorPublicKeyDTO.getSignature()));

        userService.updateUser(user);
        return toUserResponse(user);
    }

    @PutMapping("/{userId}/attribute-update-key")
    @PreAuthorize("hasRole('ROLE_AUTHORITY')")
    public UserResponse updateAttributeKey(
            @PathVariable("userId") String userId,
            @Valid @RequestBody AttributeValueUpdateKeyDTO attributeValueUpdateKeyDTO) {
        User user = userService.findUser(userId).orElseThrow(
                () -> new NotFoundException(userId)
        );

        if(! user.getAuthorityId().equals(authenticationFacade.getId())) {
            throw new ServiceException("Wrong authority.", HttpStatus.FORBIDDEN);
        }

        user.addAttributeValueUpdateKey(attributeValueUpdateKeyDTO);

        userService.updateUser(user);
        return toUserResponse(user);
    }

    @PutMapping("/{userId}/devices/{deviceId}")
    @PreAuthorize("hasRole('ROLE_AUTHORITY')")
    public DeviceResponse updateDevice(
            @PathVariable("userId") String userId,
            @PathVariable("deviceId") String deviceId,
            @Valid @RequestBody DeviceUpdateRequest deviceUpdateRequest) {

        User user = userService.findUser(userId).orElseThrow(
                () -> new NotFoundException(userId)
        );

        if(! authenticationFacade.getId().equals(user.getAuthorityId())) {
            throw new ServiceException("User does not relay in your domain.", HttpStatus.FORBIDDEN);
        }

        Set<EncryptedAttributeValueKey> encryptedAttributeValueKeySet = deviceUpdateRequest.getEncryptedAttributeValueKeys()
                .stream()
                .map(dto -> new EncryptedAttributeValueKey(dto.getAttributeValueId(), dto.getEncryptedKey()))
                .collect(Collectors.toSet());

        Device device = user.findDevice(deviceId).orElseThrow(
                () -> new ServiceException("Device with id [%s] does not exist.", HttpStatus.UNPROCESSABLE_ENTITY, deviceId)
        );

        device.setEncryptedKey(deviceUpdateRequest.getEncryptedKey());
        device.setDeviceState(deviceUpdateRequest.getDeviceState());
        device.setAttributeValueKeys(encryptedAttributeValueKeySet);
        userService.updateUser(user);

        return toDeviceResponse(device);
    }

    private UserResponse toUserResponse(User user) {
        Optional<TwoFactorPublicKey> twoFactorPublicKeyOptional = Optional.ofNullable(user.getTwoFactorPublicKey());

        return new UserResponse(
                user.getId(),
                user.getAuthorityId(),
                twoFactorPublicKeyOptional.map(twoFactorPublicKey ->
                        new TwoFactorPublicKeyDTO(
                                twoFactorPublicKey.getTwoFactorAuthenticationPublicKey(),
                                twoFactorPublicKey.getSignature()
                        )).orElse(null),
                user.getAttributeValueUpdateKeys(),
                user.getDevices().stream().map(this::toDeviceResponse).collect(Collectors.toSet())
        );
    }

    private DeviceResponse toDeviceResponse(Device device) {
        return new DeviceResponse(
                device.getCertificateId(),
                device.getDeviceState(),
                device.getEncryptedKey(),
                device.getAttributeValueKeys().stream().map(this::toEncryptedAttributeValueKeyDTO).collect(Collectors.toSet())
        );
    }

    private EncryptedAttributeValueKeyDTO toEncryptedAttributeValueKeyDTO(EncryptedAttributeValueKey encryptedAttributeValueKey) {
        return new EncryptedAttributeValueKeyDTO(
                encryptedAttributeValueKey.getAttributeValueId(),
                encryptedAttributeValueKey.getEncryptedKey()
        );
    }
}
