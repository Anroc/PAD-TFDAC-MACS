package de.tuberlin.tfdacmacs.lib.attributes.data;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PublicAttribute extends AbstractAttribute<PublicAttributeValue> {

    protected PublicAttribute(String authorityDomain, String name,
            Set<PublicAttributeValue> values, AttributeType type) {
        super(authorityDomain, name, values, type);
    }
}
