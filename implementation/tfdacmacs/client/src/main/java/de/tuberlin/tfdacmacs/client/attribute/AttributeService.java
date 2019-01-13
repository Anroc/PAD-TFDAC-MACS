package de.tuberlin.tfdacmacs.client.attribute;

import de.tuberlin.tfdacmacs.client.attribute.data.Attribute;
import de.tuberlin.tfdacmacs.client.attribute.db.AttributeDB;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public Set<Attribute> retrieveAttributes(String email, String certificateId) {
        this.attributes = attributeClient.getAttributes(email, certificateId);
        this.attributes.forEach(attribute -> attributeDB.upsert(attribute.getId(), attribute));
        return this.attributes;
    }
}
