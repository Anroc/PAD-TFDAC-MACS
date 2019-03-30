package de.tuberlin.tfdacmacs.crypto.benchmark.pairing;

import de.tuberlin.tfdacmacs.crypto.benchmark.Group;
import de.tuberlin.tfdacmacs.crypto.pairing.ABEDecryptor;
import de.tuberlin.tfdacmacs.crypto.pairing.ABEEncryptor;
import de.tuberlin.tfdacmacs.crypto.pairing.AttributeValueKeyGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.PairingCryptEngine;
import de.tuberlin.tfdacmacs.crypto.pairing.aes.AESDecryptor;
import de.tuberlin.tfdacmacs.crypto.pairing.aes.AESEncryptor;
import de.tuberlin.tfdacmacs.crypto.pairing.data.*;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.*;
import de.tuberlin.tfdacmacs.crypto.pairing.util.HashGenerator;
import de.tuberlin.tfdacmacs.crypto.rsa.StringSymmetricCryptEngine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ABEGroup extends Group<ABEUser, ABECipherText> {

    private final PairingCryptEngine pairingCryptEngine;
    private final GlobalPublicParameter gpp;
    private final DNFAccessPolicy dnfAccessPolicy;

    private final List<AttributeValueKey> attributeValueKeys;
    private final AuthorityKey authorityKey;

    private final AttributeValueKeyGenerator attributeValueKeyGenerator;

    public ABEGroup(GlobalPublicParameter globalPublicParameter,
            DNFAccessPolicy dnfAccessPolicy,
            List<AttributeValueKey> attributeValueKeys,
            AuthorityKey authorityKey) {

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
        this.attributeValueKeys = new ArrayList<>(attributeValueKeys);
        this.authorityKey = authorityKey;
    }

    @Override
    protected ABECipherText doEncrypt(byte[] content, Set<ABEUser> members, ABEUser asMember) {
        return new ABECipherText(
                pairingCryptEngine.encrypt(
                    content,
                    dnfAccessPolicy,
                    gpp,
                    asMember.asDataOwner()
        ));
    }

    @Override
    protected byte[] doDecrypt(ABECipherText abeCipherText, ABEUser asMember) {
        DNFCipherText content = abeCipherText.getCipherText();
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

    @Override
    protected void doJoin(ABEUser newMember, Set<ABEUser> existingMembers, Set<ABECipherText> cipherTexts) {
        Set<UserAttributeSecretComponent> collect = attributeValueKeys.stream().map(
                attributeValueKey -> {
                    UserAttributeValueKey userAttributeValueKey = attributeValueKeyGenerator
                            .generateUserKey(gpp, newMember.getId(), authorityKey.getPrivateKey(), attributeValueKey.getPrivateKey());
                    return new UserAttributeSecretComponent(userAttributeValueKey, attributeValueKey.getPublicKey(),
                            attributeValueKey.getAttributeValueId());
                }).collect(Collectors.toSet());
        newMember.setAttributes(collect);
    }

    @Override
    protected void doLeave(ABEUser leavingMember, Set<ABEUser> existingMembers, Set<ABECipherText> cipherTexts) {
        List<VersionedID> attributesToRevoke = leavingMember.getAttributes().stream()
                .map(UserAttributeSecretComponent::getAttributeValueId).collect(Collectors.toList());

        for ( VersionedID attributeToRevoke : attributesToRevoke) {
            AttributeValueKey attributeValueKey = findAttributeValueKey(attributeToRevoke.getId());
            AttributeValueKey attributeValueKeyNext = attributeValueKeyGenerator.generateNext(gpp, attributeValueKey);

            attributeValueKeys.remove(attributeValueKey);
            attributeValueKeys.add(attributeValueKeyNext);

            // user update
            for( ABEUser abeUser : existingMembers) {
                UserAttributeValueUpdateKey userAttributeValueUpdateKey = attributeValueKeyGenerator
                        .generateUserUpdateKey(gpp, abeUser.getId(), attributeValueKey.getPrivateKey(),
                                attributeValueKeyNext.getPrivateKey());

                UserAttributeSecretComponent userAttributeSecretComponent = abeUser.getAttributes().stream()
                        .filter(uasc -> uasc.getAttributeValueId().equals(attributeToRevoke)).findAny().get();

                userAttributeSecretComponent.getUserSecretAttributeKey().update(userAttributeValueUpdateKey);
            }

            // ct update
            Set<ABECipherText> updatedABECT = new HashSet<>();
            for( ABECipherText abeCipherText : getCipherTexts()) {
                List<CipherText> updatedCT = new ArrayList<>();
                for (CipherText cipherText : abeCipherText.getCipherText().getCipherTexts()) {
                    CipherTextAttributeUpdateKey cipherTextAttributeUpdateKey = attributeValueKeyGenerator
                            .generateCipherTextUpdateKey(cipherText, attributeValueKey, attributeValueKeyNext, null);

                    AndAccessPolicy andAccessPolicy = new AndAccessPolicy(
                            cipherText.getAccessPolicy()
                            .stream()
                            .map(accessPolicy -> new AttributePolicyElement(authorityKey.getPublicKey(), findAttributeValueKey(accessPolicy.getId()).getPublicKey()))
                            .collect(Collectors.toSet())
                    );

                    updatedCT.add(pairingCryptEngine.update(cipherText, andAccessPolicy, cipherTextAttributeUpdateKey, gpp));
                }

                updatedABECT.add(new ABECipherText(new DNFCipherText(updatedCT, abeCipherText.getCipherText().getFile())));
            }

            setCipherTexts(updatedABECT);
        }
    }

    private AttributeValueKey findAttributeValueKey(String attributeValueId) {
        return attributeValueKeys.stream()
                .filter(avk -> avk.getAttributeValueId().equals(attributeValueId))
                .findAny()
                .get();
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
}
