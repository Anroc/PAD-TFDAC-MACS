package de.tuberlin.tfdacmacs.client.decrypt;

import de.tuberlin.tfdacmacs.client.attribute.AttributeService;
import de.tuberlin.tfdacmacs.client.config.StandardStreams;
import de.tuberlin.tfdacmacs.client.csp.CSPService;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@ShellComponent
@RequiredArgsConstructor
public class DecryptionCommand {

    private final CSPService cspService;
    private final AttributeService attributeService;

    private final StandardStreams standardStreams;

    @ShellMethod("Check for new files")
    public void check() {
        standardStreams.out(String.format("%s:\t%s", "IDs", "Attributes"));
        cspService.checkForDecryptableFiles(attributeService.getAttributes())
                .forEach(
                        ct -> standardStreams.out(String.format("%s:\t%s", ct.getId(), Arrays.toString(ct.getAccessPolicy().toArray())))
                );
    }

    @ShellMethod("Check for new files and decrypt them")
    public void decrypt(String dest) {
        Path fileDestination = Paths.get(dest);
    }
}