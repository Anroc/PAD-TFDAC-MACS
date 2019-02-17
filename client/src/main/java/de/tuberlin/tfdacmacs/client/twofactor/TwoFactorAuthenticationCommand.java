package de.tuberlin.tfdacmacs.client.twofactor;

import de.tuberlin.tfdacmacs.client.config.StandardStreams;
import de.tuberlin.tfdacmacs.client.twofactor.data.PublicTwoFactorAuthentication;
import de.tuberlin.tfdacmacs.client.twofactor.data.TwoFactorAuthentication;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorKey;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.Set;
import java.util.stream.Stream;

@ShellComponent
@RequiredArgsConstructor
public class TwoFactorAuthenticationCommand {

    private final TwoFactorAuthenticationService twoFactorAuthenticationService;
    private final StandardStreams standardStreams;

    @ShellMethod(value = "Trust a user", key = "2fa trust")
    public void trust(@ShellOption(help = "comma separated emails") String users) {
        String[] userIds = extractUserIds(users);
        TwoFactorAuthentication twoFactorAuthentication = twoFactorAuthenticationService
                .trust(userIds);

        printTrustedUsers(twoFactorAuthentication);
    }

    private void printTrustedUsers(TwoFactorAuthentication twoFactorAuthentication) {
        standardStreams.out("UserIds:");
        twoFactorAuthentication.getTwoFactorKey().getPublicKeys().keySet().forEach(standardStreams::out);
    }

    @ShellMethod(value = "Distrust users", key = "2fa distrust")
    public void distrust(@ShellOption(help = "comma seperated emails") String users) {
        String[] userIds = extractUserIds(users);
        TwoFactorAuthentication twoFactorAuthentication = twoFactorAuthenticationService
                .distrust(userIds);
        printTrustedUsers(twoFactorAuthentication);
    }

    @ShellMethod(value = "Download/Update all secret two factor keys", key = "2fa update")
    public void update() {
        twoFactorAuthenticationService.update();
    }

    @ShellMethod(value = "List all stored secret and private two factor keys", key = "2fa list")
    public void list(
            @ShellOption(defaultValue = "false") boolean issued,
            @ShellOption(defaultValue = "false") boolean granted) {
        boolean all = ! issued && ! granted;

        if(all || issued) {
            standardStreams.out("Two-Factor issued by you to users:");
            twoFactorAuthenticationService.findTwoFactorAuthentication()
                    .map(TwoFactorAuthentication::getTwoFactorKey)
                    .map(TwoFactorKey::getPublicKeyValues)
                    .map(Set::stream)
                    .orElseGet(Stream::empty)
                    .map(TwoFactorKey.Public::getUserId)
                    .forEach(standardStreams::out);
        }

        if(all || granted) {
            standardStreams.out("Granted Two-Factor authentications from users:");
            twoFactorAuthenticationService.getAllPublicTwoFactorAuthentications()
                    .stream()
                    .map(PublicTwoFactorAuthentication::getOwnerId)
                    .forEach(standardStreams::out);
        }
    }

    private String[] extractUserIds(String users) {
        if(users.isEmpty()) {
            throw new IllegalArgumentException("users should be a comma separated list of user ids");
        }

        return users.split(",");
    }

}
