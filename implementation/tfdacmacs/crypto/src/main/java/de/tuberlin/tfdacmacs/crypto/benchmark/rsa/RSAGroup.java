package de.tuberlin.tfdacmacs.crypto.benchmark.rsa;

import de.tuberlin.tfdacmacs.crypto.benchmark.Group;
import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.StringSymmetricCryptEngine;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.security.Key;
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
    protected RSACipherText doEncrypt(byte[] content, Set<RSAUser> members, RSAUser asMember) {
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
    public Group reset() {
        init();
        return super.reset();
    }
}
