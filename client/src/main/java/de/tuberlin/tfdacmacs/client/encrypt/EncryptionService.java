package de.tuberlin.tfdacmacs.client.encrypt;

import de.tuberlin.tfdacmacs.client.csp.CSPService;
import de.tuberlin.tfdacmacs.client.encrypt.data.EncryptedFile;
import de.tuberlin.tfdacmacs.client.gpp.GPPService;
import de.tuberlin.tfdacmacs.crypto.pairing.PairingCryptEngine;
import de.tuberlin.tfdacmacs.crypto.pairing.data.CipherText;
import de.tuberlin.tfdacmacs.crypto.pairing.data.DNFAccessPolicy;
import de.tuberlin.tfdacmacs.crypto.pairing.data.DNFCipherText;
import de.tuberlin.tfdacmacs.crypto.pairing.data.DataOwner;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EncryptionService {

    private final PairingCryptEngine pairingCryptEngine;
    private final GPPService gppService;
    private final CSPService cspService;

    public void encrypt(@NonNull Path plainText, @NonNull DNFAccessPolicy dnfAccessPolicy) {
        encrypt(plainText, dnfAccessPolicy, null);
    }

    public void encrypt(@NonNull Path plainText, @NonNull DNFAccessPolicy dnfAccessPolicy, DataOwner dataOwner) {
        try {
            DNFCipherText dnfCipherText = pairingCryptEngine
                    .encrypt(Files.readAllBytes(plainText), dnfAccessPolicy, gppService.getGPP(), dataOwner);

            List<CipherText> cipherTexts = dnfCipherText.getCipherTexts();
            cspService.createCipherTexts(cipherTexts);
            log.info("FileId: {} for file: {}", dnfCipherText.getFile().getId(), plainText.toFile().getName());
            cspService.uploadFile(EncryptedFile.from(dnfCipherText.getFile(), plainText.toFile().getName()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
