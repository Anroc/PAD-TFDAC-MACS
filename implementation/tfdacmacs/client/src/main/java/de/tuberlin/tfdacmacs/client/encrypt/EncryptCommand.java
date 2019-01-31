package de.tuberlin.tfdacmacs.client.encrypt;

import de.tuberlin.tfdacmacs.client.twofactor.TwoFactorAuthenticationService;
import de.tuberlin.tfdacmacs.crypto.pairing.data.DNFAccessPolicy;
import de.tuberlin.tfdacmacs.crypto.pairing.policy.AccessPolicyParser;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.nio.file.Path;
import java.nio.file.Paths;

@ShellComponent
@RequiredArgsConstructor
public class EncryptCommand {

    private final AccessPolicyParser accessPolicyParser;
    private final EncryptionService encryptionService;
    private final TwoFactorAuthenticationService twoFactorAuthenticationService;

    @ShellMethod("Encrypt a file")
    public void encrypt(String file,
            @ShellOption(help = "only selected users can decrypt", defaultValue = "false", value = "2fa") boolean twoFactor,
            @ShellOption(help = "Boolean formula in DNF form.") String policy) {
        DNFAccessPolicy dnfAccessPolicy = accessPolicyParser.parse(policy);
        Path plainText = Paths.get(file);
        if(twoFactor) {
            encryptionService.encrypt(plainText, dnfAccessPolicy, twoFactorAuthenticationService.getDataOwner());
        } else {
            encryptionService.encrypt(plainText, dnfAccessPolicy);
        }
    }
}
