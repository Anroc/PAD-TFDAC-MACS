package de.tuberlin.tfdacmacs.client.decrypt;

import de.tuberlin.tfdacmacs.client.attribute.AttributeService;
import de.tuberlin.tfdacmacs.client.decrypt.client.DecryptionClient;
import de.tuberlin.tfdacmacs.client.encrypt.data.EncryptedFile;
import de.tuberlin.tfdacmacs.client.gpp.GPPService;
import de.tuberlin.tfdacmacs.client.rest.session.Session;
import de.tuberlin.tfdacmacs.client.twofactor.TwoFactorAuthenticationService;
import de.tuberlin.tfdacmacs.client.twofactor.data.PublicTwoFactorAuthentication;
import de.tuberlin.tfdacmacs.crypto.pairing.PairingCryptEngine;
import de.tuberlin.tfdacmacs.crypto.pairing.data.CipherText;
import de.tuberlin.tfdacmacs.crypto.pairing.data.UserAttributeSecretComponent;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DecryptionService {

    private final PairingCryptEngine pairingCryptEngine;
    private final GPPService gppService;

    private final AttributeService attributeService;
    private final TwoFactorAuthenticationService twoFactorAuthenticationService;
    private final Session session;

    private final DecryptionClient decryptionClient;

    public Path decrypt(@NonNull Path fileDestination, @NonNull CipherText cipherText) {
        String fileId = cipherText.getFileId();
        EncryptedFile encryptedFile = decryptionClient.getFile(fileId);

        Optional<TwoFactorKey.Public> twoFactorAuthenticationKey = Optional.empty();
        if(cipherText.isTwoFactorSecured()) {
            twoFactorAuthenticationKey = twoFactorAuthenticationService
                    .findPublicTwoFactorAuthentication(cipherText.getOwnerId().getId())
                    .map(PublicTwoFactorAuthentication::getTwoFactorKey);
        }

        Set<UserAttributeSecretComponent> components = attributeService.getUserAttributeSecretComponents(cipherText.getAccessPolicy());
        byte[] plainTextFile = pairingCryptEngine.decrypt(
                encryptedFile.getData(),
                cipherText,
                gppService.getGPP(),
                session.getEmail(),
                components,
                twoFactorAuthenticationKey.orElse(null));

        fileDestination.toFile().mkdirs();
        Path filePath = fileDestination.resolve(encryptedFile.getFileName());

        try {
            Files.write(filePath, plainTextFile);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return filePath;
    }
}
