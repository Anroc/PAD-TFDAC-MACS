package de.tuberlin.tfdacmacs.attributeauthority.attributes;

import de.tuberlin.tfdacmacs.attributeauthority.attributes.db.AttributeDB;
import de.tuberlin.tfdacmacs.attributeauthority.config.AttributeAuthorityConfig;
import de.tuberlin.tfdacmacs.attributeauthority.gpp.GlobalPublicParameterService;
import de.tuberlin.tfdacmacs.basics.attributes.data.Attribute;
import de.tuberlin.tfdacmacs.basics.attributes.data.AttributeType;
import de.tuberlin.tfdacmacs.basics.attributes.data.AttributeValue;
import de.tuberlin.tfdacmacs.basics.crypto.AttributeKeyGenerator;
import de.tuberlin.tfdacmacs.basics.gpp.data.GlobalPublicParameter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttributeService {

    private final AttributeDB attributeDB;
    private final AttributeKeyGenerator attributeKeyGenerator;
    private final GlobalPublicParameterService globalPublicParameterService;
    private final AttributeAuthorityConfig config;

    public Collection<Attribute> findAllAttributes() {
        return attributeDB.findAll();
    }

    public Optional<Attribute> findAttribute(@NonNull String attributeId) {
        return attributeDB.findEntity(attributeId);
    }

    public <T> Attribute createAttribute(@NonNull String name, @NonNull AttributeType type, List<T> values) {
        GlobalPublicParameter gpp = globalPublicParameterService.createOrRetrieveGPP();

        List<AttributeValue<T>> attrValues = values.stream().map(value ->
                        attributeKeyGenerator.generateAttributeKeys(value, gpp))
                        .collect(Collectors.toList());

        return new Attribute(config.getId(), name, attrValues, type);
    }
}
