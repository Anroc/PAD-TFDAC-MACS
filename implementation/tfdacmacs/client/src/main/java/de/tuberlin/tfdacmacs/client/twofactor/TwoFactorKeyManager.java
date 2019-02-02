package de.tuberlin.tfdacmacs.client.twofactor;

import de.tuberlin.tfdacmacs.client.gpp.GPPService;
import de.tuberlin.tfdacmacs.client.twofactor.client.TwoFactorAuthenticationClient;
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
    private final TwoFactorAuthenticationClient client;

    public TwoFactorKey generate(String... userIds) {
        TwoFactorKey twoFactorKey = twoFactorKeyGenerator.generate(gppService.getGPP());
        Arrays.stream(userIds).forEach(userId -> generatePublicKeyForUser(twoFactorKey, userId));
        return twoFactorKey;
    }

    public void generatePublicKeyForUser(@NonNull TwoFactorKey twoFactorKey, @NonNull String userId) {
        twoFactorKey = twoFactorKeyGenerator
                .generatePublicKeyForUser(gppService.getGPP(), twoFactorKey, userId);

        TwoFactorKey.Public publicKeyOfUser = twoFactorKey.getPublicKeyOfUser(userId);

        client.uploadTwoFactorKey(publicKeyOfUser);
    }
}
