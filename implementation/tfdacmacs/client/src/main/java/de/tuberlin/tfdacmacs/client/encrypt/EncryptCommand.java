package de.tuberlin.tfdacmacs.client.encrypt;

import de.tuberlin.tfdacmacs.client.policy.AccessPolicyParser;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
@RequiredArgsConstructor
public class EncryptCommand {

    private final AccessPolicyParser accessPolicyParser;

    @ShellMethod("Encrypt a file")
    public void encrypt(String file,
            @ShellOption(help = "comma seperated emails", defaultValue = ShellOption.NULL) String emails,
            @ShellOption(help = "Boolean formula in DNF form.") String policy) {
        accessPolicyParser.parse(policy);
    }
}
