package de.tuberlin.tfdacmacs.client.twofactor;

import de.tuberlin.tfdacmacs.client.attribute.client.AttributeClient;
import de.tuberlin.tfdacmacs.client.csp.client.CipherTextClient;
import de.tuberlin.tfdacmacs.client.gpp.GPPService;
import de.tuberlin.tfdacmacs.client.twofactor.client.TwoFactorAuthenticationClient;
import de.tuberlin.tfdacmacs.client.twofactor.data.TwoFactorAuthentication;
import de.tuberlin.tfdacmacs.crypto.pairing.TwoFactorKeyGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.data.CipherText;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.CipherText2FAUpdateKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorUpdateKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TwoFactorKeyManager {

    private final GPPService gppService;
    private final TwoFactorKeyGenerator twoFactorKeyGenerator;
    private final TwoFactorAuthenticationClient twoFactorClient;
    private final CipherTextClient cipherTextClient;
    private final AttributeClient attributeClient;

    public TwoFactorKey generate(String... userIds) {
        TwoFactorKey twoFactorKey = twoFactorKeyGenerator.generate(gppService.getGPP());
        Arrays.stream(userIds).forEach(userId -> generatePublicKeyForUser(twoFactorKey, userId));
        return twoFactorKey;
    }

    public String generatePublicKeyForUser(@NonNull TwoFactorKey twoFactorKey, @NonNull String userId) {
        twoFactorKey = twoFactorKeyGenerator
                .generatePublicKeyForUser(gppService.getGPP(), twoFactorKey, userId);

        TwoFactorKey.Public publicKeyOfUser = twoFactorKey.getPublicKeyOfUser(userId);

        return twoFactorClient.uploadTwoFactorKey(publicKeyOfUser);
    }

    public TwoFactorAuthentication update(TwoFactorAuthentication twoFactorAuthentication) {
        TwoFactorKey.Private revokedMasterKey = twoFactorAuthentication.getTwoFactorKey().getPrivateKey();

        TwoFactorKey twoFactorKey = twoFactorKeyGenerator.generate(gppService.getGPP());
        twoFactorKey.getPublicKeys().putAll(twoFactorAuthentication.getTwoFactorKey().getPublicKeys());
        twoFactorAuthentication.setTwoFactorKey(twoFactorKey);

        List<TwoFactorUpdateKey> user2FAUpdateKeys =
                generateUserUpdateKeys(twoFactorAuthentication, revokedMasterKey);
        List<CipherText2FAUpdateKey> cipherText2FAUpdateKeys =
                generateCipherTextUpdateKeys(twoFactorAuthentication, revokedMasterKey);

        twoFactorClient.updateTwoFactorKeys(user2FAUpdateKeys);
        cipherTextClient.updateCipherText(twoFactorAuthentication.getOwnerId(), cipherText2FAUpdateKeys);

        return calculateLocalUpdate(twoFactorAuthentication, user2FAUpdateKeys);
    }

    private TwoFactorAuthentication calculateLocalUpdate(TwoFactorAuthentication twoFactorAuthentication,
            List<TwoFactorUpdateKey> user2FAUpdateKeys) {
        user2FAUpdateKeys.forEach(twoFactorUpdateKey ->
                twoFactorAuthentication.getTwoFactorKey().getPublicKeyOfUser(twoFactorUpdateKey.getUserId()).update(twoFactorUpdateKey)
        );

        return twoFactorAuthentication;
    }

    private List<TwoFactorUpdateKey> generateUserUpdateKeys(TwoFactorAuthentication twoFactorAuthentication, TwoFactorKey.Private revokedMasterKey) {
        return twoFactorAuthentication.getTwoFactorKey().getPublicKeys().keySet()
                .stream()
                .map(userId -> update(revokedMasterKey, twoFactorAuthentication.getTwoFactorKey().getPrivateKey(), userId))
                .collect(Collectors.toList());
    }

    private List<CipherText2FAUpdateKey> generateCipherTextUpdateKeys(TwoFactorAuthentication twoFactorAuthentication, TwoFactorKey.Private revokedMasterKey) {
        return cipherTextClient.getCipherTexts(twoFactorAuthentication.getOwnerId())
                .stream()
                .map(CipherText::getAccessPolicy)
                .flatMap(Set::stream)
                .distinct()
                .map(attributeClient::findAttributePublicKey)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map((attributeValuePublicKey) ->
                        twoFactorKeyGenerator.generateCipherTextUpdateKey(
                                revokedMasterKey,
                                twoFactorAuthentication.getTwoFactorKey().getPrivateKey(),
                                attributeValuePublicKey,
                                twoFactorAuthentication.getOwnerId()
                        )
                ).collect(Collectors.toList());
    }

    private TwoFactorUpdateKey update(
            TwoFactorKey.Private revokedMasterKey,
            TwoFactorKey.Private masterKey,
            String userId) {
        return twoFactorKeyGenerator.generateUpdateKey(
                gppService.getGPP(),
                masterKey,
                revokedMasterKey,
                userId
        );
    }
}