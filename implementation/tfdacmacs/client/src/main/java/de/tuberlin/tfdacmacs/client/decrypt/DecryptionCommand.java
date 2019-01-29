package de.tuberlin.tfdacmacs.client.decrypt;

import de.tuberlin.tfdacmacs.client.attribute.AttributeService;
import de.tuberlin.tfdacmacs.client.config.StandardStreams;
import de.tuberlin.tfdacmacs.client.csp.CSPService;
import de.tuberlin.tfdacmacs.crypto.pairing.data.CipherText;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ShellComponent
@RequiredArgsConstructor
public class DecryptionCommand {

    private final CSPService cspService;
    private final AttributeService attributeService;
    private final DecryptionService decryptionService;

    private final StandardStreams standardStreams;

    private List<CipherText> cipherTexts = new ArrayList<>();

    @ShellMethod("Check for new files")
    public void check() {
        this.cipherTexts = cspService.checkForDecryptableFiles(attributeService.getAttributes());
        standardStreams.out(String.format("%s:\t%s", "IDs", "Attributes"));
        for (int i = 0; i < cipherTexts.size(); i++) {
            CipherText ct = cipherTexts.get(i);
            standardStreams.out(String.format("[%d]: %s:\t%s", i+1, ct.getId(), Arrays.toString(ct.getAccessPolicy().toArray())));
        }
    }

    @ShellMethod("Check for new files and decrypt them")
    public void decrypt(String dest, int ct) {
        Path fileDestination = Paths.get(dest);
        CipherText cipherText = getCipherText(ct);

        Path decryptedFile = decryptionService.decrypt(fileDestination, cipherText);

        standardStreams.out("Successfully decrypted: " + decryptedFile.toFile().getAbsolutePath());
    }

    private CipherText getCipherText(int ct) {
        if(ct-1 >= cipherTexts.size()) {
            throw new IllegalArgumentException("Given cipher text number is out of range. Did you forget to call 'check'?");
        }

        return cipherTexts.get(ct);
    }
}
