package de.tuberlin.tfdacmacs.client.twofactor;

import de.tuberlin.tfdacmacs.client.attribute.client.AttributeClient;
import de.tuberlin.tfdacmacs.client.csp.client.CipherTextClient;
import de.tuberlin.tfdacmacs.client.gpp.GPPService;
import de.tuberlin.tfdacmacs.client.twofactor.client.TwoFactorAuthenticationClient;
import de.tuberlin.tfdacmacs.client.twofactor.data.TwoFactorAuthentication;
import de.tuberlin.tfdacmacs.crypto.pairing.TwoFactorKeyGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.data.CipherText;
import de.tuberlin.tfdacmacs.crypto.pairing.data.VersionedID;
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
        TwoFactorKey twoFactorKey = twoFactorKeyGenerator.generateNew(gppService.getGPP());
        Arrays.stream(userIds).forEach(userId -> generatePublicKeyForUser(twoFactorKey, userId));
        update2FAPublicKey(twoFactorKey);
        return twoFactorKey;
    }

    private void update2FAPublicKey(TwoFactorKey twoFactorKey) {
        twoFactorClient.updateUserForTwoFactorPublicKey(twoFactorKey.getPublicKey());
    }

    public String generatePublicKeyForUser(@NonNull TwoFactorKey twoFactorKey, @NonNull String userId) {
        twoFactorKey = twoFactorKeyGenerator
                .generatePublicKeyForUser(gppService.getGPP(), twoFactorKey, userId);

        TwoFactorKey.Secret secretKeyOfUser = twoFactorKey.getSecretKeyOfUser(userId);

        return twoFactorClient.uploadTwoFactorKey(secretKeyOfUser);
    }

    public TwoFactorAuthentication update(
            @NonNull TwoFactorAuthentication twoFactorAuthentication,
            @NonNull Set<String> revokedUserIds) {
        TwoFactorKey.Private revokedMasterKey = twoFactorAuthentication.getTwoFactorKey().getPrivateKey();

        TwoFactorKey twoFactorKey = twoFactorKeyGenerator.generateNext(gppService.getGPP(), twoFactorAuthentication.getTwoFactorKey());
        twoFactorKey.getSecrets().putAll(twoFactorAuthentication.getTwoFactorKey().getSecrets());
        twoFactorAuthentication.setTwoFactorKey(twoFactorKey);

        List<TwoFactorUpdateKey> user2FAUpdateKeys =
                generateUserUpdateKeys(twoFactorAuthentication, revokedMasterKey);
        List<CipherText2FAUpdateKey> cipherText2FAUpdateKeys =
                generateCipherTextUpdateKeys(twoFactorAuthentication, revokedMasterKey);

        update2FAPublicKey(twoFactorKey);
        twoFactorClient.updateTwoFactorKeys(user2FAUpdateKeys);
        cipherTextClient.updateCipherText(twoFactorAuthentication.getOwnerId(), revokedMasterKey.getVersion(), cipherText2FAUpdateKeys);
        twoFactorClient.deleteTwoFactorKeys(revokedUserIds);

        return calculateLocalUpdate(twoFactorAuthentication, user2FAUpdateKeys);
    }

    private TwoFactorAuthentication calculateLocalUpdate(TwoFactorAuthentication twoFactorAuthentication,
            List<TwoFactorUpdateKey> user2FAUpdateKeys) {
        user2FAUpdateKeys.forEach(twoFactorUpdateKey -> {
                    TwoFactorKey.Secret tfUpdated = twoFactorAuthentication.getTwoFactorKey()
                            .getSecretKeyOfUser(twoFactorUpdateKey.getUserId()).update(twoFactorUpdateKey);
                    twoFactorAuthentication.getTwoFactorKey().putPublicKey(tfUpdated.getUserId(), tfUpdated);
                }
        );

        return twoFactorAuthentication;
    }

    private List<TwoFactorUpdateKey> generateUserUpdateKeys(TwoFactorAuthentication twoFactorAuthentication, TwoFactorKey.Private revokedMasterKey) {
        return twoFactorAuthentication.getTwoFactorKey().getSecrets().keySet()
                .stream()
                .map(userId -> update(revokedMasterKey, twoFactorAuthentication.getTwoFactorKey().getPrivateKey(), userId))
                .collect(Collectors.toList());
    }

    private List<CipherText2FAUpdateKey> generateCipherTextUpdateKeys(TwoFactorAuthentication twoFactorAuthentication, TwoFactorKey.Private revokedMasterKey) {
        return cipherTextClient.getCipherTexts(twoFactorAuthentication.getOwnerId())
                .stream()
                .map(CipherText::getAccessPolicy)
                .flatMap(Set::stream)
                .map(VersionedID::getId)
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
                revokedMasterKey,
                masterKey,
                userId
        );
    }
}
