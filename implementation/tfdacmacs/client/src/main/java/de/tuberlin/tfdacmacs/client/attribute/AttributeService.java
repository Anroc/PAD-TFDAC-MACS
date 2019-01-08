package de.tuberlin.tfdacmacs.client.attribute;

import de.tuberlin.tfdacmacs.client.attribute.data.Attribute;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AttributeService {

    private final AttributeClient attributeClient;

    @Getter
    private Set<Attribute> attributes;

    public Set<Attribute> retrieveAttributes(String email, String certificateId) {
        this.attributes = attributeClient.getAttributes(email, certificateId);
        return this.attributes;
    }
}
