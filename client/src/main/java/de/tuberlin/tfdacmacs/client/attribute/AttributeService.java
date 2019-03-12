package de.tuberlin.tfdacmacs.client.attribute;

import de.tuberlin.tfdacmacs.client.attribute.client.AttributeClient;
import de.tuberlin.tfdacmacs.client.attribute.data.Attribute;
import de.tuberlin.tfdacmacs.client.attribute.db.AttributeDB;
import de.tuberlin.tfdacmacs.client.attribute.exceptions.InvalidAttributeValueIdentifierException;
import de.tuberlin.tfdacmacs.client.register.events.LogoutEvent;
import de.tuberlin.tfdacmacs.crypto.pairing.data.UserAttributeSecretComponent;
import de.tuberlin.tfdacmacs.crypto.pairing.data.VersionedID;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.pairing.policy.AttributeValueKeyProvider;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttributeService implements AttributeValueKeyProvider {

    private final AttributeClient attributeClient;
    private final AttributeDB attributeDB;

    private Set<Attribute> attributes;

    public Set<Attribute> getAttributes() {
        if(attributes == null) {
            this.attributes = attributeDB.findAll().collect(Collectors.toSet());
        }
        return this.attributes;
    }

    @EventListener(LogoutEvent.class)
    public void cleanState() {
        this.attributes = null;
    }

    public Set<Attribute> retrieveAttributesForUser(String email, String certificateId) {
        this.attributes = attributeClient.getAttributesForUser(email, certificateId);
        this.attributes.forEach(attribute -> attributeDB.upsert(attribute.getId(), attribute));
        return this.attributes;
    }

    public Optional<AttributeValueKey.Public> findAttributeValuePublicKey(@NonNull String attributeValueId) {
        return attributeClient.findAttributePublicKey(attributeValueId);
    }

    @Override
    public AttributeValueKey.Public getAttributeValuePublicKey(@NonNull String attributeValueId) {
        return findAttributeValuePublicKey(attributeValueId).orElseThrow(
                () -> new InvalidAttributeValueIdentifierException(attributeValueId)
        );
    }

    public Set<UserAttributeSecretComponent> getUserAttributeSecretComponents(@NonNull Set<VersionedID> attributeIds) {
        return getAttributes().stream()
                .filter(attribute -> attributeIds.contains(attribute.asVersionedID()))
                .map(attribute -> new UserAttributeSecretComponent(
                        attribute.getUserAttributeValueKey(),
                        getAttributeValuePublicKey(attribute.getId()),
                        attribute.getId()))
                .collect(Collectors.toSet());
    }
}
