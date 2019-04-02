package de.tuberlin.tfdacmacs.crypto.benchmark.rsa;

import de.tuberlin.tfdacmacs.crypto.benchmark.Group;
import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.StringSymmetricCryptEngine;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RSAGroup extends Group<RSAUser, RSACipherText> {

    private StringAsymmetricCryptEngine asymmetricCryptEngine;
    private StringSymmetricCryptEngine symmetricCryptEngine;

    public RSAGroup() {
        init();
    }

    private final void init() {
        this.asymmetricCryptEngine = new StringAsymmetricCryptEngine();
        this.symmetricCryptEngine = new StringSymmetricCryptEngine();
    }

    @Override
    protected RSACipherText doEncrypt(byte[] content, List<RSAUser> members, RSAUser asMember) {
        Key symmetricCipherKey = symmetricCryptEngine.getSymmetricCipherKey();

        return new RSACipherText(
                members.stream().collect(Collectors.toMap(
                    member -> member.getId(),
                    member -> {
                        try {
                            return new EncryptedFile(
                                    asymmetricCryptEngine.encryptRaw(symmetricCipherKey.getEncoded(), member.getPublicKey()),
                                    symmetricCryptEngine.encryptRaw(content, symmetricCipherKey)
                            );
                        } catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
                            throw new RuntimeException(e);
                        }
                    }))
        );
    }

    @Override
    protected byte[] doDecrypt(RSACipherText rsaCipherText, RSAUser asMember) {
        Map<String, EncryptedFile> content = rsaCipherText.getEncryptedFile();
        EncryptedFile encryptedFile = content.get(asMember.getId());
        try{
            byte[] bytes = asymmetricCryptEngine.decryptRaw(encryptedFile.getFileKey(), asMember.getPrivateKey());
            return symmetricCryptEngine.decryptRaw(encryptedFile.getData(), symmetricCryptEngine.createKeyFromBytes(bytes));
        } catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doJoin(RSAUser newMember, List<RSAUser> existingMembers, Set<RSACipherText> cipherTexts) {
        RSAUser existingUser = existingMembers.get(0);
        for (RSACipherText cipherText : cipherTexts) {
            EncryptedFile encryptedFile = cipherText.getEncryptedFile().get(existingUser.getId());
            try {
                String decrypt = asymmetricCryptEngine.decrypt(encryptedFile.getFileKey(), existingUser.getPrivateKey());
                String encrypt = asymmetricCryptEngine.encrypt(decrypt, newMember.getPublicKey());
                cipherText.getEncryptedFile().put(newMember.getId(), new EncryptedFile(encrypt, encryptedFile.getData()));
            } catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void doLeave(RSAUser leavingMember, List<RSAUser> existingMembers, Set<RSACipherText> cipherTexts) {
        cipherTexts.stream()
                .forEach(ct -> ct.getEncryptedFile().remove(leavingMember.getId()));
    }

    @Override
    public Group reset() {
        init();
        return super.reset();
    }
}
