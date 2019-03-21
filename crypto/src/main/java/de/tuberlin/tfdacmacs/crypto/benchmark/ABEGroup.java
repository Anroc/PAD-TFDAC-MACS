package de.tuberlin.tfdacmacs.crypto.benchmark;

import de.tuberlin.tfdacmacs.crypto.pairing.ABEDecryptor;
import de.tuberlin.tfdacmacs.crypto.pairing.ABEEncryptor;
import de.tuberlin.tfdacmacs.crypto.pairing.AttributeValueKeyGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.PairingCryptEngine;
import de.tuberlin.tfdacmacs.crypto.pairing.aes.AESDecryptor;
import de.tuberlin.tfdacmacs.crypto.pairing.aes.AESEncryptor;
import de.tuberlin.tfdacmacs.crypto.pairing.data.*;
import de.tuberlin.tfdacmacs.crypto.pairing.util.HashGenerator;
import de.tuberlin.tfdacmacs.crypto.rsa.StringSymmetricCryptEngine;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ABEGroup extends Group<ABEUser, DNFCipherText> {

    private final PairingCryptEngine pairingCryptEngine;
    private final GlobalPublicParameter gpp;
    private final DNFAccessPolicy dnfAccessPolicy;
    private final AttributeValueKeyGenerator attributeValueKeyGenerator;

    public ABEGroup(GlobalPublicParameter globalPublicParameter, DNFAccessPolicy dnfAccessPolicy) {
        StringSymmetricCryptEngine symmetricCryptEngine = new StringSymmetricCryptEngine();
        HashGenerator hashGenerator = new HashGenerator();

        AESEncryptor aesEncryptor = new AESEncryptor(hashGenerator, symmetricCryptEngine);
        AESDecryptor aesDecryptor = new AESDecryptor(hashGenerator, symmetricCryptEngine);
        ABEEncryptor abeEncryptor = new ABEEncryptor();
        ABEDecryptor abeDecryptor = new ABEDecryptor(hashGenerator);

        this.pairingCryptEngine = new PairingCryptEngine(
                aesEncryptor,
                aesDecryptor,
                abeEncryptor,
                abeDecryptor
        );

        this.attributeValueKeyGenerator = new AttributeValueKeyGenerator(hashGenerator);
        this.gpp = globalPublicParameter;
        this.dnfAccessPolicy = dnfAccessPolicy;
    }

    @Override
    protected DNFCipherText doEncrypt(byte[] content, Set<ABEUser> members, ABEUser asMember) {
        return pairingCryptEngine.encrypt(
                content,
                dnfAccessPolicy,
                gpp,
                asMember.asDataOwner()
        );
    }

    @Override
    protected byte[] doDecrypt(DNFCipherText content, ABEUser asMember) {
        CipherText suitableCipherText = findSuitableCipherText(content, asMember);

        return pairingCryptEngine.decrypt(
                content.getFile().getData(),
                suitableCipherText,
                gpp,
                asMember.getId(),
                asMember.getAttributes(),
                asMember.getTwoFactorPublicKey(suitableCipherText.getOwnerId().getId())
        );
    }

    private CipherText findSuitableCipherText(DNFCipherText dnfCipherText, ABEUser asMember) {
        Set<VersionedID> attriubteIds =
                asMember.getAttributes().stream().map(UserAttributeSecretComponent::getAttributeValueId)
                        .collect(Collectors.toSet());

        return dnfCipherText.getCipherTexts()
                .stream()
                .filter(ct -> attriubteIds.containsAll(ct.getAccessPolicy()))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalStateException("User does not satisfy any given CT.")
                );
    }

    @Override
    protected void doLeave(ABEUser member, Set<ABEUser> members) {
        attributeValueKeyGenerator.gene
    }

    @Override
    protected void doJoin(ABEUser member, Set<ABEUser> members) {

    }

    @Override protected List<CipherText> getCipherTexts() {
        return null;
    }
}
