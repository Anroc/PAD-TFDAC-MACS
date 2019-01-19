package de.tuberlin.tfdacmacs.client.encrypt;

import de.tuberlin.tfdacmacs.crypto.pairing.policy.AccessPolicyParser;
import de.tuberlin.tfdacmacs.crypto.pairing.data.DNFAccessPolicy;
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

    @ShellMethod("Encrypt a file")
    public void encrypt(String file,
            @ShellOption(help = "comma seperated emails", defaultValue = ShellOption.NULL) String emails,
            @ShellOption(help = "Boolean formula in DNF form.") String policy) {
        DNFAccessPolicy dnfAccessPolicy = accessPolicyParser.parse(policy);
        Path plainText = Paths.get(file);
        if(emails != null) {
            String[] uids = emails.split(",");
            encryptionService.encrypt(plainText, dnfAccessPolicy, uids);
        } else {
            encryptionService.encrypt(plainText, dnfAccessPolicy);
        }
    }
}
