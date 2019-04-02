package de.tuberlin.tfdacmacs.crypto.benchmark.factory;

import de.tuberlin.tfdacmacs.crypto.benchmark.User;
import de.tuberlin.tfdacmacs.crypto.benchmark.pairing.ABEUser;
import de.tuberlin.tfdacmacs.crypto.pairing.AttributeValueKeyGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.TwoFactorKeyGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.UserAttributeSecretComponent;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AuthorityKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.UserAttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.pairing.util.HashGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ABEUserFactory extends UserFactory<ABEUser> {

    private final AttributeValueKeyGenerator attributeValueKeyGenerator;
    private final TwoFactorKeyGenerator twoFactorKeyGenerator;
    private final AuthorityKey.Private authorityPrivateKey;
    private final List<AttributeValueKey> attributesPerUser;
    private final GlobalPublicParameter gpp;

    public ABEUserFactory(GlobalPublicParameter gpp, AuthorityKey.Private authorityPrivateKey, List<AttributeValueKey> attributesPerUser) {
        HashGenerator hashGenerator = new HashGenerator();

        this.attributeValueKeyGenerator = new AttributeValueKeyGenerator(hashGenerator);
        this.twoFactorKeyGenerator = new TwoFactorKeyGenerator(hashGenerator);
        this.attributesPerUser = attributesPerUser;
        this.authorityPrivateKey = authorityPrivateKey;
        this.gpp = gpp;
    }

    @Override
    public List<ABEUser> create(int num) {

        List<ABEUser> users = new ArrayList<>();
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

        String[] uids = users.stream().map(User::getId).toArray(String[]::new);
        TwoFactorKey twoFactorKey = twoFactorKeyGenerator.generateNew(gpp, uids);
        ABEUser abeUser = users.get(0);
        abeUser.setTwoFactorKey(twoFactorKey);
        users.forEach(user -> user.getTfPublics().put(abeUser.getId(), abeUser.getTwoFactorKey().getSecretKeyOfUser(user.getId())));

        return users;
    }
}
