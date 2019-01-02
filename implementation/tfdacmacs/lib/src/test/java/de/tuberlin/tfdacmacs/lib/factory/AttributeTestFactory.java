package de.tuberlin.tfdacmacs.lib.factory;

import de.tuberlin.tfdacmacs.lib.attributes.data.Attribute;
import de.tuberlin.tfdacmacs.lib.attributes.data.AttributeType;
import lombok.RequiredArgsConstructor;
import org.assertj.core.util.Sets;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AttributeTestFactory {

    private final AttributeValueTestFactory attributeValueTestFactory;

    public Attribute create() {
        return new Attribute("aa.tu-berlin.de", "testAttribute", Sets.newLinkedHashSet(
                attributeValueTestFactory.createString()
        ), AttributeType.STRING);
    }
}
