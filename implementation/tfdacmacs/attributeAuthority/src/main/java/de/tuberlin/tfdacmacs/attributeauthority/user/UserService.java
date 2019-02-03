package de.tuberlin.tfdacmacs.attributeauthority.user;

import de.tuberlin.tfdacmacs.attributeauthority.attribute.AttributeService;
import de.tuberlin.tfdacmacs.attributeauthority.authority.AuthorityKeyProvider;
import de.tuberlin.tfdacmacs.attributeauthority.gpp.GPPProvider;
import de.tuberlin.tfdacmacs.attributeauthority.user.client.UserClient;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.UserAttributeKey;
import de.tuberlin.tfdacmacs.attributeauthority.user.db.UserDB;
import de.tuberlin.tfdacmacs.attributeauthority.user.events.UserCreatedEvent;
import de.tuberlin.tfdacmacs.crypto.pairing.AttributeValueKeyGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AuthorityKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.UserAttributeValueKey;
import de.tuberlin.tfdacmacs.lib.attributes.data.Attribute;
import de.tuberlin.tfdacmacs.lib.attributes.data.AttributeValue;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final GPPProvider gppProvider;
    private final AttributeService attributeService;
    private final AuthorityKeyProvider authorityKeyProvider;
    private final UserClient userClient;

    private final UserDB userDB;

    private final AttributeValueKeyGenerator attributeValueKeyGenerator;

    public Optional<User> findUser(@NonNull String userId) {
        return findUser(userId, true);
    }

    public Optional<User> findUser(@NonNull String userId, boolean extendWithCa) {
        Optional<User> user = userDB.findEntity(userId);
        if(extendWithCa) {
            return user.map(this::extendWithCAUser);
        }
        return user;
    }

    public boolean existUser(@NonNull String userId) {
        return userDB.exist(userId);
    }

    public User extendWithCAUser(User user) {
        user = userClient.extendWithCAUser(user);
        updateUser(user);
        return user;
    }

    public User updateUser(@NonNull User user) {
        userDB.update(user);
        return user;
    }

    public User createUser(@NonNull String id, Set<UserAttributeKey> attributes) {
        User user = new User(id);
        enrichWithUserAttributeSecretKeys(id, attributes);
        user.setAttributes(attributes);
        user.registerDomainEvent(new UserCreatedEvent(user));
        userDB.insert(user);

        return user;
    }

    private Set<UserAttributeKey> enrichWithUserAttributeSecretKeys(String userId, Set<UserAttributeKey> attributes) {
        log.info("Generating secret keys for user {}", userId);
        GlobalPublicParameter gpp = gppProvider.getGlobalPublicParameter();
        AuthorityKey.Private authorityPrivateKey = authorityKeyProvider.getPrivateKey();

        return attributes.stream().map(
                userAttributeKey -> {
                    userAttributeKey.setKey(
                            generateSecretUserAttributeValueKey(userId, gpp, authorityPrivateKey, userAttributeKey)
                    );
                    return userAttributeKey;
                }
        ).collect(Collectors.toSet());
    }

    private UserAttributeValueKey generateSecretUserAttributeValueKey(String userId, GlobalPublicParameter gpp,
            AuthorityKey.Private authorityPrivateKey, UserAttributeKey userAttributeKey) {
        AttributeValue attributeValue = getAttributeValue(gpp, userAttributeKey);

        return attributeValueKeyGenerator.generateUserKey(gpp, userId,
                authorityPrivateKey, attributeValue.getPrivateKey());
    }

    private AttributeValue getAttributeValue(GlobalPublicParameter gpp, UserAttributeKey userAttributeKey) {
        Attribute attribute = attributeService.findAttribute(userAttributeKey.getAttributeId())
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Could not find attribute with id [%s]", userAttributeKey.getAttributeId())));
        return attributeService
                .getOrCreateAttributeKey(attribute, userAttributeKey.getValue(), gpp);
    }

    public User approve(User user, String deviceId) {
        user = user.approve(deviceId);
        userDB.update(user);
        return user;
    }
}
