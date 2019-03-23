package de.tuberlin.tfdacmacs.crypto.benchmark.factory;

import de.tuberlin.tfdacmacs.crypto.benchmark.pairing.ABEUser;
import de.tuberlin.tfdacmacs.crypto.pairing.AttributeValueKeyGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.UserAttributeSecretComponent;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AuthorityKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.UserAttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.pairing.util.HashGenerator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ABEUserFactory extends UserFactory<ABEUser> {

    private final AttributeValueKeyGenerator attributeValueKeyGenerator;
    private final AuthorityKey.Private authorityPrivateKey;
    private final List<AttributeValueKey> attributesPerUser;
    private final GlobalPublicParameter gpp;

    public ABEUserFactory(GlobalPublicParameter gpp, AuthorityKey.Private authorityPrivateKey, List<AttributeValueKey> attributesPerUser) {
        this.attributeValueKeyGenerator = new AttributeValueKeyGenerator(new HashGenerator());
        this.attributesPerUser = attributesPerUser;
        this.authorityPrivateKey = authorityPrivateKey;
        this.gpp = gpp;
    }

    @Override
    public Set<ABEUser> create(int num) {

        Set<ABEUser> users = new HashSet<>();
        for (int i = 0; i < num; i++) {
            String userId = UUID.randomUUID().toString();
            Set<UserAttributeSecretComponent> userAttributeSecretComponents = attributesPerUser.stream().map(attributeValueKey -> {
                UserAttributeValueKey userAttributeValueKey = attributeValueKeyGenerator
                        .generateUserKey(gpp, userId, authorityPrivateKey, attributeValueKey.getPrivateKey());
                UserAttributeSecretComponent userAttributeSecretComponent = new UserAttributeSecretComponent(
                        userAttributeValueKey,
                        attributeValueKey.getPublicKey(),
                        attributeValueKey.getAttributeValueId());
                return userAttributeSecretComponent;
            }).collect(Collectors.toSet());
            users.add(new ABEUser(userId, userAttributeSecretComponents));
        }
        return users;
    }
}
