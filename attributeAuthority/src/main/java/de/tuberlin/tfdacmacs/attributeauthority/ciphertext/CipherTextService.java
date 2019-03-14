package de.tuberlin.tfdacmacs.attributeauthority.ciphertext;

import de.tuberlin.tfdacmacs.attributeauthority.ciphertext.client.CipherTextClient;
import de.tuberlin.tfdacmacs.crypto.pairing.AttributeValueKeyGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.data.CipherText;
import de.tuberlin.tfdacmacs.crypto.pairing.data.VersionedID;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.CipherTextAttributeUpdateKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CipherTextService {

    private final AttributeValueKeyGenerator attributeValueKeyGenerator;
    private final CipherTextClient cipherTextClient;

    public void updateCipherTexts(@NonNull AttributeValueKey revokedAttributeValueKey, @NonNull AttributeValueKey newAttributeValueKey) {
        List<CipherText> cipherTexts = cipherTextClient.findCipherTextsByAttribute(revokedAttributeValueKey.getAttributeValueId());

        Set<VersionedID> ownerIds = cipherTexts.stream()
                .filter(CipherText::isTwoFactorSecured)
                .map(CipherText::getOwnerId)
                .collect(Collectors.toSet());

        Map<String, TwoFactorKey.Public> twoFactorPublicKeys = cipherTextClient.findTwoFactorPublicKeys(ownerIds);

        Map<String, CipherTextAttributeUpdateKey> cipherTestUpdates = cipherTexts.stream().collect(
                Collectors.toMap(
                        cipherText -> cipherText.getId(),
                        cipherText -> attributeValueKeyGenerator
                                .generateCipherTextUpdateKey(
                                        cipherText,
                                        revokedAttributeValueKey,
                                        newAttributeValueKey,
                                        cipherText.isTwoFactorSecured() ? twoFactorPublicKeys.get(cipherText.getOwnerId().getId()) : null
                                )
                ));

        cipherTextClient.updateCipherTexts(cipherTestUpdates);
    }
}
