package de.tuberlin.tfdacmacs.client.register;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
@RequiredArgsConstructor
public class RegisterCommand {

    private final RegistrationService registrationService;

    @ShellMethod("Register this client")
    public void register(String email) {
        registrationService.certificateRequest(email);
    }
}
