package de.tuberlin.tfdacmacs.attributeauthority.attribute;

import de.tuberlin.tfdacmacs.attributeauthority.attribute.db.AttributeDB;
import de.tuberlin.tfdacmacs.attributeauthority.config.AttributeAuthorityConfig;
import de.tuberlin.tfdacmacs.attributeauthority.init.gpp.GPPService;
import de.tuberlin.tfdacmacs.lib.attributes.data.Attribute;
import de.tuberlin.tfdacmacs.lib.attributes.data.AttributeType;
import de.tuberlin.tfdacmacs.lib.attributes.data.AttributeValue;
import de.tuberlin.tfdacmacs.crypto.pairing.AttributeValueKeyGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttributeService {

    private final AttributeDB attributeDB;
    private final AttributeValueKeyGenerator attributeValueKeyGenerator;
    private final GPPService gppService;
    private final AttributeAuthorityConfig config;

    public Collection<Attribute<AttributeValue>> findAllAttributes() {
        return attributeDB.findAll();
    }

    public Optional<Attribute<AttributeValue>> findAttribute(@NonNull String attributeId) {
        return attributeDB.findEntity(attributeId);
    }

    public Attribute createAttribute(@NonNull String name, @NonNull AttributeType type, List<?> values) {
        GlobalPublicParameter gpp = gppService.getGlobalPublicParameter();

        Set<AttributeValue> attrValues = values.stream()
                .map(value -> generateAttributeKeys(value, AttributeValue.generateId(config.getId(), name, value), gpp))
                .collect(Collectors.toSet());

        Attribute attribute = Attribute.createAttribute(config.getId(), name, attrValues, type);
        attributeDB.insert(attribute);
        return attribute;
    }

    public <T> AttributeValue<T> getOrCreateAttributeKey(@NonNull Attribute<AttributeValue> attribute, @NonNull T value, @NonNull GlobalPublicParameter gpp) {
        Optional<AttributeValue> res = attribute.getValues().stream()
                .filter(attributeValue -> attributeValue.getValue().equals(value))
                .findAny();
        if (res.isPresent()) {
            return res.get();
        }

        log.info("Generating new attribute key for value {} of attribute {}.", value, attribute.getId());
        AttributeValue<T> newAttributeValue = generateAttributeKeys(value, AttributeValue.generateId(attribute, value), gpp);
        attribute.addValue(newAttributeValue);
        attributeDB.update(attribute);
        return newAttributeValue;
    }

    /**
     * Generates new attribute private and public keys for the given attribute value.
     *
     * @param value the attribute value
     * @param gpp the global public parameter
     * @param <T> the type of the attribute
     * @return the computed {@link AttributeValue}
     */
    private <T> AttributeValue<T> generateAttributeKeys(
            @NonNull T value,
            @NonNull String attributeValueId,
            @NonNull GlobalPublicParameter gpp) {

        AttributeValueKey key = attributeValueKeyGenerator.generate(gpp, attributeValueId);
        return new AttributeValue(value, key);
    }
}
