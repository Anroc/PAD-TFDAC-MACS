package de.tuberlin.tfdacmacs.basics.crypto.pairing;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.*;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.util.HashGenerator;
import de.tuberlin.tfdacmacs.basics.crypto.rsa.StringSymmetricCryptEngine;
import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class PairingCryptEngine {

    private final StringSymmetricCryptEngine symmetricCryptEngine;
    private final HashGenerator hashGenerator;

    public CipherText encrypt(
            byte[] data,
            @NonNull AndAccessPolicy andAccessPolicy,
            @NonNull GlobalPublicParameter gpp,
            DataOwner dataOwner) {
        CipherTextDescription cipherTextDescription = abeEncrypt(andAccessPolicy, gpp, dataOwner);
        String encryptedMessage = aesEncrypt(data, cipherTextDescription.getKey());
        return cipherTextDescription.toCipherText(encryptedMessage);
    }

    private String aesEncrypt(byte[] data, Element key) {
        Key symmetricKey = generateAesKey(key);
        try {
            return symmetricCryptEngine.encryptRaw(data, symmetricKey);
        } catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }

    protected CipherTextDescription abeEncrypt(
            @NonNull AndAccessPolicy andAccessPolicy,
            @NonNull GlobalPublicParameter gpp,
            DataOwner dataOwner) {
        Element key = gpp.getPairing().getGT().newRandomElement().getImmutable();
        Element s = gpp.getPairing().getZr().newRandomElement().getImmutable();

        Map<Element, Set<Element>> policy = andAccessPolicy.groupByAttributeAuthority();

        Element c1 = null;
        Element c2 = gpp.getG().powZn(s).getImmutable();
        Element c3 = null;
        for(Map.Entry<Element, Set<Element>> entry : policy.entrySet()) {
            Element authorityPublicKey = entry.getKey().duplicate();
            int n = entry.getValue().size();

            c1 = mulOrDefault(c1, authorityPublicKey.pow(BigInteger.valueOf(n)));

            for( Element attributePublicKey : entry.getValue()) {
                c3 = mulOrDefault(c3, attributePublicKey);
            }
        }

        c1 = key.duplicate().mul(c1.duplicate().powZn(s)).getImmutable();

        if(dataOwner == null) {
            c3 = c3.powZn(s).getImmutable();
            return new CipherTextDescription(c1, c2, c3, andAccessPolicy, null, key);
        } else {
            c3 = c3.powZn(s.duplicate().add(dataOwner.getTwoFactorPrivateKey())).getImmutable();
            return new CipherTextDescription(c1, c2, c3, andAccessPolicy, dataOwner.getId(), null, key);
        }
    }

    public byte[] decrypt(
            @NonNull CipherText cipherText,
            @NonNull GlobalPublicParameter gpp,
            @NonNull String userId,
            @NonNull Set<AttributeSecretComponents> secrets,
            Element twoFactorPrivateKey) {

        Element key = abeDecrypt(cipherText, gpp, userId, secrets, twoFactorPrivateKey);
        return aesDecrypt(cipherText.getEncryptedMessage(), key);
    }

    private byte[] aesDecrypt(String encryptedMessage, Element key) {
        Key symmetricKey = generateAesKey(key);
        try {
            return symmetricCryptEngine.decryptRaw(encryptedMessage, symmetricKey);
        } catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            log.error("Decryption failed. Maybe wrong key is used.", e);
            return new byte[0];
        }
    }

    private Key generateAesKey(Element key) {
        byte[] randomBytes = hashGenerator.sha256Hash(key.toBytes(), 32);
        return symmetricCryptEngine.createKeyFromBytes(randomBytes);
    }

    protected Element abeDecrypt(@NonNull CipherText cipherText, @NonNull GlobalPublicParameter gpp,
            @NonNull String userId, @NonNull Set<AttributeSecretComponents> secrets, Element twoFactorPrivateKey) {
        Element sk = secrets.stream()
                .map(AttributeSecretComponents::getUserSecretAttributeKey)
                .reduce((a,b) -> a.duplicate().mul(b))
                .orElseThrow(() -> new IllegalArgumentException("Given secrets where empty."));

        Element upk = secrets.stream()
                .map(AttributeSecretComponents::getAttributePublicKey)
                .reduce((a,b) -> a.duplicate().mul(b))
                .orElseThrow(() -> new IllegalArgumentException("Given secrets where empty."));

        Element pairing1 = gpp.getPairing().pairing(hashGenerator.g1Hash(gpp, userId), cipherText.getC3());
        Element upper = cipherText.getC1().duplicate().mul(pairing1);

        Element pairing2 = gpp.getPairing().pairing(cipherText.getC2(), sk);

        if(twoFactorPrivateKey == null) {
            return upper.div(pairing2);
        } else {
            Element pairing3 = gpp.getPairing().pairing(twoFactorPrivateKey, upk);
            Element lower = pairing2.mul(pairing3);
            return upper.div(lower);
        }
    }

    private Element mulOrDefault(Element target, @NonNull Element multiplier) {
        if(target == null) {
            return multiplier;
        } else {
            return target.mul(multiplier);
        }
    }
}
