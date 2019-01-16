package de.tuberlin.tfdacmacs.centralserver.attribute;

import de.tuberlin.tfdacmacs.centralserver.attribute.db.PublicAttributeDB;
import de.tuberlin.tfdacmacs.lib.attributes.data.PublicAttribute;
import de.tuberlin.tfdacmacs.lib.attributes.data.PublicAttributeValue;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PublicAttributeService {

    private final PublicAttributeDB publicAttributeDB;

    public void insertEntity(@NonNull PublicAttribute attribute) {
        publicAttributeDB.insert(attribute);
    }

    public List<PublicAttribute> findAll() {
        return publicAttributeDB.findAll();
    }

    public Optional<PublicAttribute> findEntity(@NonNull String id) {
        return publicAttributeDB.findEntity(id);
    }

    public PublicAttribute addValue(@NonNull PublicAttribute publicAttribute, @NonNull PublicAttributeValue publicAttributeValue) {
        publicAttribute.addValue(publicAttributeValue);
        publicAttributeDB.update(publicAttribute);
        return publicAttribute;
    }
}
