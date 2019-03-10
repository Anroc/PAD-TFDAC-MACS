package de.tuberlin.tfdacmacs.attributeauthority.attribute;

import de.tuberlin.tfdacmacs.attributeauthority.attribute.db.AttributeDB;
import de.tuberlin.tfdacmacs.attributeauthority.config.AttributeAuthorityConfig;
import de.tuberlin.tfdacmacs.attributeauthority.gpp.GPPProvider;
import de.tuberlin.tfdacmacs.crypto.pairing.AttributeValueKeyGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.UserAttributeValueUpdateKey;
import de.tuberlin.tfdacmacs.lib.attributes.data.Attribute;
import de.tuberlin.tfdacmacs.lib.attributes.data.AttributeType;
import de.tuberlin.tfdacmacs.lib.attributes.data.AttributeValue;
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
    private final GPPProvider gppProvider;
    private final AttributeAuthorityConfig config;

    public Collection<Attribute> findAllAttributes() {
        return attributeDB.findAll();
    }

    public Optional<Attribute> findAttribute(@NonNull String attributeId) {
        return attributeDB.findEntity(attributeId);
    }

    public Attribute createAttribute(@NonNull String name, @NonNull AttributeType type, List<?> values) {
        GlobalPublicParameter gpp = gppProvider.getGlobalPublicParameter();

        Set<AttributeValue> attrValues = values.stream()
                .map(value -> generateNewAttributeKey(value, AttributeValue.generateId(config.getId(), name, value), gpp))
                .collect(Collectors.toSet());

        Attribute attribute = Attribute.createAttribute(config.getId(), name, attrValues, type);
        attributeDB.insert(attribute);
        return attribute;
    }

    public <T> AttributeValue<T> getOrCreateAttributeKey(@NonNull Attribute attribute, @NonNull T value, @NonNull GlobalPublicParameter gpp) {
        Optional<AttributeValue> res = attribute.getValues().stream()
                .filter(attributeValue -> attributeValue.getValue().equals(value))
                .findAny();
        if (res.isPresent()) {
            return res.get();
        }

        log.info("Generating new attribute key for value {} of attribute {}.", value, attribute.getId());
        AttributeValue<T> newAttributeValue = generateNewAttributeKey(value, AttributeValue.generateId(attribute, value), gpp);
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
    private <T> AttributeValue<T> generateNewAttributeKey(
            @NonNull T value,
            @NonNull String attributeValueId,
            @NonNull GlobalPublicParameter gpp) {
        AttributeValueKey key = attributeValueKeyGenerator.generateNew(gpp, attributeValueId);
        return new AttributeValue(value, key);
    }

    private <T> AttributeValue<T> generateNextAttributeKey(
            @NonNull T value,
            @NonNull AttributeValueKey attributeValueKey,
            @NonNull GlobalPublicParameter gpp) {
        AttributeValueKey key = attributeValueKeyGenerator.generateNext(gpp, attributeValueKey);
        return new AttributeValue(value, key);
    }

    public void revoke(@NonNull Attribute attribute, @NonNull String attributeValueId) {
        GlobalPublicParameter gpp = gppProvider.getGlobalPublicParameter();

        AttributeValue attributeValue = attribute.findAttributeValue(attributeValueId)
                .orElseThrow(() -> new IllegalStateException("Could not find attribute value with id: " + attributeValueId));

        AttributeValue newAttributeValue = generateNextAttributeKey(
                attributeValue.getValue(),
                attributeValue,
                gpp
        );

        attribute.updateValue(newAttributeValue);
        attributeDB.update(attribute);
    }

    public UserAttributeValueUpdateKey generateUpdateKey(
            @NonNull String userId,
            @NonNull AttributeValue revokedAttributeValue,
            @NonNull AttributeValue newAttributeValue) {
        GlobalPublicParameter gpp = gppProvider.getGlobalPublicParameter();

        return attributeValueKeyGenerator.generateUserUpdateKey(
                gpp,
                userId,
                revokedAttributeValue.getPrivateKey(),
                newAttributeValue.getPrivateKey());
    }
}
