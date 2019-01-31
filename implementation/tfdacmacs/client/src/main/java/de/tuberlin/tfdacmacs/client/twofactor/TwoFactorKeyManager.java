package de.tuberlin.tfdacmacs.client.twofactor;

import de.tuberlin.tfdacmacs.client.gpp.GPPService;
import de.tuberlin.tfdacmacs.crypto.pairing.TwoFactorKeyGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class TwoFactorKeyManager {

    private final GPPService gppService;
    private final TwoFactorKeyGenerator twoFactorKeyGenerator;

    public TwoFactorKey generate(String... userIds) {
        TwoFactorKey twoFactorKey = twoFactorKeyGenerator.generate(gppService.getGPP());
        Arrays.stream(userIds).forEach(userId -> generatePublicKeyForUser(twoFactorKey, userId));
        return twoFactorKey;
    }

    public void generatePublicKeyForUser(@NonNull TwoFactorKey twoFactorKey, @NonNull String userId) {
        twoFactorKey = twoFactorKeyGenerator
                .generatePublicKeyForUser(gppService.getGPP(), twoFactorKey, userId);

        TwoFactorKey.Public publicKeyOfUser = twoFactorKey.getPublicKeyOfUser(userId);

        // TODO:
        // 1. request user object from CA
        // 2. extract aid and cert from user
        // 3. validate aid in trusted AIDs
        // 4. calculate cert Id and request AA for certificate validation
        // 5. encrypt and sign Public key with device Public key and upload to CA
    }
}
