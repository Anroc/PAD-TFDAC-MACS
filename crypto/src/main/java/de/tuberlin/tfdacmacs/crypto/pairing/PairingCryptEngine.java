package de.tuberlin.tfdacmacs.crypto.pairing;

import de.tuberlin.tfdacmacs.crypto.pairing.aes.AESDecryptor;
import de.tuberlin.tfdacmacs.crypto.pairing.aes.AESEncryptor;
import de.tuberlin.tfdacmacs.crypto.pairing.data.*;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.CipherTextAttributeUpdateKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorKey;
import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class PairingCryptEngine {

    private final AESEncryptor aesEncryptor;
    private final AESDecryptor aesDecryptor;
    private final ABEEncryptor abeEncrypt;
    private final ABEDecryptor abeDecryptor;

    public DNFCipherText encrypt(
            byte[] data,
            @NonNull DNFAccessPolicy dnfAccessPolicy,
            @NonNull GlobalPublicParameter gpp,
            DataOwner dataOwner) {
        AndAccessPolicy andAccessPolicy = dnfAccessPolicy.getAndAccessPolicies().get(0);
        CipherTextDescription cipherTextDescription = abeEncrypt.encrypt(andAccessPolicy, gpp, dataOwner, null);
        byte[] encryptedContent = aesEncryptor.encrypt(data, cipherTextDescription.getKey());
        File file = new File(encryptedContent);

        List<CipherText> cipherTexts = new ArrayList<>();
        cipherTexts.add(cipherTextDescription.bindTo(file));

        for(int i = 1; i< dnfAccessPolicy.getAndAccessPolicies().size(); i++) {
            andAccessPolicy = dnfAccessPolicy.getAndAccessPolicies().get(i);
            CipherTextDescription additionalCTDescription = abeEncrypt.encrypt(
                    andAccessPolicy,
                    gpp,
                    dataOwner,
                    cipherTextDescription.getKey());

            cipherTexts.add(additionalCTDescription.bindTo(file));
        }

        return new DNFCipherText(cipherTexts, file);
    }


    public AndCipherText encrypt(
            byte[] data,
            @NonNull AndAccessPolicy andAccessPolicy,
            @NonNull GlobalPublicParameter gpp,
            DataOwner dataOwner) {
        CipherTextDescription cipherTextDescription = abeEncrypt.encrypt(andAccessPolicy, gpp, dataOwner, null);
        byte[] encryptedContent = aesEncryptor.encrypt(data, cipherTextDescription.getKey());
        File file = new File(encryptedContent);
        return new AndCipherText(cipherTextDescription.bindTo(file), file);
    }

    public byte[] decrypt(
            byte[] encryptedContent,
            @NonNull CipherText cipherText,
            @NonNull GlobalPublicParameter gpp,
            @NonNull String userId,
            @NonNull Set<UserAttributeSecretComponent> secrets,
            TwoFactorKey.Public twoFactorPublicKey) {

        Element key = abeDecryptor.decrypt(cipherText, gpp, userId, secrets, twoFactorPublicKey);
        return aesDecryptor.decrypt(encryptedContent, key);
    }

    public CipherText update(
            @NonNull CipherText cipherText,
            @NonNull AndAccessPolicy andAccessPolicy,
            @NonNull CipherTextAttributeUpdateKey cipherTextAttributeUpdateKey,
            @NonNull GlobalPublicParameter gpp) {
        return abeEncrypt.update(gpp, cipherText, andAccessPolicy, cipherTextAttributeUpdateKey);
    }
}
