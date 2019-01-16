package de.tuberlin.tfdacmacs.client.attribute;

import de.tuberlin.tfdacmacs.client.attribute.data.Attribute;
import de.tuberlin.tfdacmacs.client.attribute.db.AttributeDB;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttributeService {

    private final AttributeClient attributeClient;
    private final AttributeDB attributeDB;

    private Set<Attribute> attributes;

    public Set<Attribute> getAttributes() {
        if(attributes == null) {
            this.attributes = attributeDB.findAll().collect(Collectors.toSet());
        }
        return this.attributes;
    }

    public Set<Attribute> retrieveAttributesForUser(String email, String certificateId) {
        this.attributes = attributeClient.getAttributesForUser(email, certificateId);
        this.attributes.forEach(attribute -> attributeDB.upsert(attribute.getId(), attribute));
        return this.attributes;
    }

    public Optional<AttributeValueKey.Public> findAttributeValuePublicKey(@NonNull String attributeValueId) {
        return attributeClient.findAttributePublicKey(attributeValueId);
    }
}
