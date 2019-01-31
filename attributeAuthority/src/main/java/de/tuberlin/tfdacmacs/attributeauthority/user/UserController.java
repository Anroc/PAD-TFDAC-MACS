package de.tuberlin.tfdacmacs.attributeauthority.user;

import de.tuberlin.tfdacmacs.attributeauthority.attribute.AttributeService;
import de.tuberlin.tfdacmacs.attributeauthority.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.UserAttributeKey;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.dto.AttributeValueRequest;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.dto.CreateUserRequest;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.dto.DeviceResponse;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.dto.UserResponse;
import de.tuberlin.tfdacmacs.lib.attributes.data.Attribute;
import de.tuberlin.tfdacmacs.lib.exceptions.BadRequestException;
import de.tuberlin.tfdacmacs.lib.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    public final UserService userService;
    public final AttributeService attributeService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        Set<UserAttributeKey> preKeys = createUserRequest.getAttributeValueRequests()
                .stream()
                .flatMap(attributeValueRequest -> validateAndExtractAttributeValueRequest(attributeValueRequest))
                .collect(Collectors.toSet());

        User user = userService.createUser(createUserRequest.getEmail(), preKeys);
        return UserResponse.from(user);
    }

    private Stream<UserAttributeKey> validateAndExtractAttributeValueRequest(AttributeValueRequest attributeValueRequest) {
        Attribute attribute = attributeService.findAttribute(attributeValueRequest.getAttributeId())
                .orElseThrow(() -> new NotFoundException(attributeValueRequest.getAttributeId()));
        for(Object value : attributeValueRequest.getAttributeValues()) {
            if( ! attribute.getType().matchesType(value) ) {
                throw new BadRequestException("Value '%s' does not match attribute type '%s'",
                        value.toString(), attribute.getType());
            }
        }
        return attributeValueRequest.getAttributeValues()
                .stream()
                .map(value -> new UserAttributeKey(attribute.getId(), value, null));
    }

    @GetMapping("/{email}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public UserResponse getAttributeKeys(@PathVariable("email") String email) {
        User user = userService.findUser(email).orElseThrow(
                () -> new NotFoundException(email)
        );
        return UserResponse.from(user);
    }

    @PutMapping("/{email}/approve/{deviceId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public UserResponse approve(@PathVariable("email") String email, @PathVariable("deviceId") String deviceId) {
        User user = userService.findUser(email).orElseThrow(
                () -> new NotFoundException(email)
        );

        userService.approve(user, deviceId);

        return UserResponse.from(user);
    }

    @GetMapping("/{email}/devices/{deviceId}")
    @PreAuthorize("hasAnyRole('ROLE_AUTHORITY', 'ROLE_USER')")
    public DeviceResponse getDevice(
            @PathVariable("email") String email,
            @PathVariable("deviceId") String deviceId) {

        User user = userService.findUser(email).orElseThrow(
                () -> new NotFoundException(email)
        );

        if(user.getDevices().stream().map(Certificate::getId).anyMatch(deviceId::equals)) {
            return new DeviceResponse(deviceId);
        } else {
            throw new NotFoundException(deviceId);
        }
    }
}
