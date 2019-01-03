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

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class PairingCryptEngine {

    private final AESEncryptor aesEncryptor;
    private final AESDecryptor aesDecryptor;
    private final ABEEncryptor abeEncrypt;
    private final ABEDecryptor abeDecryptor;


    public CipherText encrypt(
            byte[] data,
            @NonNull AndAccessPolicy andAccessPolicy,
            @NonNull GlobalPublicParameter gpp,
            DataOwner dataOwner) {
        CipherTextDescription cipherTextDescription = abeEncrypt.encrypt(andAccessPolicy, gpp, dataOwner);
        String encryptedMessage = aesEncryptor.encrypt(data, cipherTextDescription.getKey());
        return cipherTextDescription.toCipherText(encryptedMessage);
    }

    public byte[] decrypt(
            @NonNull CipherText cipherText,
            @NonNull GlobalPublicParameter gpp,
            @NonNull String userId,
            @NonNull Set<UserAttributeSecretComponents> secrets,
            TwoFactorKey.Public twoFactorPublicKey) {

        Element key = abeDecryptor.decrypt(cipherText, gpp, userId, secrets, twoFactorPublicKey);
        return aesDecryptor.decrypt(cipherText.getEncryptedMessage(), key);
    }

    public CipherText update(
            @NonNull CipherText cipherText,
            @NonNull AndAccessPolicy andAccessPolicy,
            @NonNull CipherTextAttributeUpdateKey cipherTextAttributeUpdateKey,
            @NonNull GlobalPublicParameter gpp) {
        return abeEncrypt.update(gpp, cipherText, andAccessPolicy, cipherTextAttributeUpdateKey);
    }
}
