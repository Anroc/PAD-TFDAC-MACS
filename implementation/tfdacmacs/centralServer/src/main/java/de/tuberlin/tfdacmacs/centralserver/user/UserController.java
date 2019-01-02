package de.tuberlin.tfdacmacs.centralserver.user;

import de.tuberlin.tfdacmacs.lib.db.exception.EntityDoesExistException;
import de.tuberlin.tfdacmacs.lib.exceptions.ServiceException;
import de.tuberlin.tfdacmacs.lib.user.data.dto.DeviceResponse;
import de.tuberlin.tfdacmacs.lib.user.data.dto.EncryptedAttributeValueKeyDTO;
import de.tuberlin.tfdacmacs.lib.user.data.dto.UserCreationRequest;
import de.tuberlin.tfdacmacs.lib.user.data.dto.UserResponse;
import de.tuberlin.tfdacmacs.centralserver.authority.AttributeAuthorityService;
import de.tuberlin.tfdacmacs.centralserver.security.AuthenticationFacade;
import de.tuberlin.tfdacmacs.centralserver.user.data.Device;
import de.tuberlin.tfdacmacs.centralserver.user.data.EncryptedAttributeValueKey;
import de.tuberlin.tfdacmacs.centralserver.user.data.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
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
        User user = new User(userCreationRequest.getId(), userCreationRequest.getAuthorityId());
        user.setAuthorityId(userCreationRequest.getAuthorityId());

        if( !attributeAuthorityService.exist(userCreationRequest.getAuthorityId())) {
            throw new ServiceException("Authority with id [%s] does not exist.", HttpStatus.UNPROCESSABLE_ENTITY, userCreationRequest.getAuthorityId());
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

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getAuthorityId(),
                user.getDevices().stream().map(this::toDeviceResponse).collect(Collectors.toSet())
        );
    }

    private DeviceResponse toDeviceResponse(Device device) {
        return new DeviceResponse(
                device.getCertificateId(),
                device.getDeviceState(),
                device.getAttributeValueKeys().stream().map(this::toEncryptedAttributeValueKeyDTO).collect(Collectors.toSet())
        );
    }

    private EncryptedAttributeValueKeyDTO toEncryptedAttributeValueKeyDTO(EncryptedAttributeValueKey encryptedAttributeValueKey) {
        return new EncryptedAttributeValueKeyDTO();// TODO implement
    }
}
