package de.tuberlin.tfdacmacs.client.twofactor;

import de.tuberlin.tfdacmacs.client.config.StandardStreams;
import de.tuberlin.tfdacmacs.client.twofactor.data.TwoFactorAuthentication;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
@RequiredArgsConstructor
public class TwoFactorAuthenticationCommand {

    private final TwoFactorAuthenticationService twoFactorAuthenticationService;
    private final StandardStreams standardStreams;

    @ShellMethod(value = "Encrypt a file", key = "2fa trust")
    public void trust(@ShellOption(help = "comma separated emails") String users) {
        if(users.isEmpty()) {
            throw new IllegalArgumentException("users should be a comma separated list of user ids");
        }

        String[] userIds = users.split(",");
        TwoFactorAuthentication twoFactorAuthentication = twoFactorAuthenticationService
                .upsertTwoFactorAuthentication(userIds);

        standardStreams.out("UserIds:");
        twoFactorAuthentication.getTwoFactorKey().getPublicKeys().keySet().forEach(standardStreams::out);
    }

    @ShellMethod(value = "Download all two factor keys", key = "2fa update")
    public void update() {
        twoFactorAuthenticationService.update();
    }
}
