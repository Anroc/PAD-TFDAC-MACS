package de.tuberlin.tfdacmacs.client.csp;

import de.tuberlin.tfdacmacs.client.attribute.data.Attribute;
import de.tuberlin.tfdacmacs.client.csp.client.CipherTextClient;
import de.tuberlin.tfdacmacs.client.encrypt.data.EncryptedFile;
import de.tuberlin.tfdacmacs.client.twofactor.TwoFactorAuthenticationService;
import de.tuberlin.tfdacmacs.crypto.pairing.data.CipherText;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CSPService {

    private final CipherTextClient cipherTextClient;
    private final TwoFactorAuthenticationService twoFactorAuthenticationService;

    public void createCipherTexts(@NonNull List<CipherText> cipherTexts) {
        cipherTextClient.bulkCreateCipherText(cipherTexts);
    }

    public void uploadFile(@NonNull EncryptedFile file) {
        cipherTextClient.createFile(file);
    }

    public List<CipherText> checkForDecryptableFiles(@NonNull Set<Attribute> attributes) {
        return cipherTextClient.getCipherTexts(
                    attributes.stream()
                            .map(Attribute::getId)
                            .collect(Collectors.toList()))
                .stream()
                .filter(ct -> {
                    if(ct.isTwoFactorSecured()) {
                        boolean isPresent = twoFactorAuthenticationService.findPublicTwoFactorAuthentication(
                                ct.getOwnerId().getId()
                        ).isPresent();

                        if (!isPresent) {
                            log.info("Ignoring cipher text [{}] since we don't have a suitable 2FA key.", ct.getId());
                        }

                        return isPresent;
                    } else {
                        return true;
                    }
                }).collect(Collectors.toList());
    }
}
