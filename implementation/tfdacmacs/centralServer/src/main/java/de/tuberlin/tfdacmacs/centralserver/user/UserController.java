package de.tuberlin.tfdacmacs.centralserver.user;

import de.tuberlin.tfdacmacs.basics.exceptions.ServiceException;
import de.tuberlin.tfdacmacs.centralserver.user.data.User;
import de.tuberlin.tfdacmacs.centralserver.user.data.dto.UserCreationRequest;
import de.tuberlin.tfdacmacs.centralserver.user.data.dto.UserCreationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserCreationResponse createUser(@Valid @RequestBody UserCreationRequest userCreationRequest) {
        if(userService.existUser(userCreationRequest.getEmail())) {
            throw new ServiceException("User with email '%s' does exist.", HttpStatus.PRECONDITION_FAILED,
                    userCreationRequest.getEmail());
        }

        User user = userService.createUser(userCreationRequest.getEmail());
        return UserCreationResponse.from(user);
    }
}
