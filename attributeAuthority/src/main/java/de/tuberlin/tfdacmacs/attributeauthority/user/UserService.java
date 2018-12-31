package de.tuberlin.tfdacmacs.attributeauthority.user;

import de.tuberlin.tfdacmacs.attributeauthority.attribute.AttributeService;
import de.tuberlin.tfdacmacs.attributeauthority.init.authority.AuthorityKeyService;
import de.tuberlin.tfdacmacs.attributeauthority.init.gpp.GPPService;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
import de.tuberlin.tfdacmacs.attributeauthority.user.data.UserAttributeKey;
import de.tuberlin.tfdacmacs.attributeauthority.user.db.UserDB;
import de.tuberlin.tfdacmacs.attributeauthority.user.events.UserCreatedEvent;
import de.tuberlin.tfdacmacs.basics.attributes.data.Attribute;
import de.tuberlin.tfdacmacs.basics.attributes.data.AttributeValue;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.AttributeValueKeyGenerator;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.AuthorityKey;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.UserAttributeValueKey;
import de.tuberlin.tfdacmacs.basics.crypto.rsa.StringAsymmetricCryptEngine;
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

    private final GPPService gppService;
    private final AttributeService attributeService;
    private final AuthorityKeyService authorityKeyService;

    private final UserDB userDB;
    private final StringAsymmetricCryptEngine cryptEngine;

    private final AttributeValueKeyGenerator attributeValueKeyGenerator;

    public Optional<User> findUser(@NonNull String userId) {
        return userDB.findEntity(userId);
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
        GlobalPublicParameter gpp = gppService.getGlobalPublicParameter();
        AuthorityKey.Private authorityPrivateKey = authorityKeyService.getPrivateKey();

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
}
