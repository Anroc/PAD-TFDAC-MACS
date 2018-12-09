package de.tuberlin.tfdacmacs.basics.crypto;

import de.tuberlin.tfdacmacs.basics.UnitTestSuite;
import de.tuberlin.tfdacmacs.basics.attributes.data.AttributeValue;
import de.tuberlin.tfdacmacs.basics.factory.GPPTestFactory;
import de.tuberlin.tfdacmacs.basics.gpp.data.GlobalPublicParameter;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AttributeKeyGeneratorTest extends UnitTestSuite {

    private GlobalPublicParameter globalPublicParameter;
    private AttributeKeyGenerator attributeKeyGenerator;

    @Before
    public void setup() {
        attributeKeyGenerator = new AttributeKeyGenerator();
        globalPublicParameter = gppTestFactory.create();
    }

    @Test
    public void generateAttributeKeys_passes_forTwoValuesGeneration() {
        AttributeValue<String> value1 = attributeKeyGenerator.generateAttributeKeys("value1", globalPublicParameter);
        AttributeValue<String> value2 = attributeKeyGenerator.generateAttributeKeys("value2", globalPublicParameter);

        assertThat(value1.getValue()).isEqualTo("value1");
        assertThat(value2.getValue()).isEqualTo("value2");
        assertThat(value1.getPublicKey().toBytes()).isNotEqualTo(value2.getPrivateKey().toBytes());
        assertThat(value1.getPublicKey().toBytes()).isNotEqualTo(value2.getPublicKey().toBytes());

    }

}