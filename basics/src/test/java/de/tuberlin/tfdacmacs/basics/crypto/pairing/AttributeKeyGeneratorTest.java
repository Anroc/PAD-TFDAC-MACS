package de.tuberlin.tfdacmacs.basics.crypto.pairing;

import de.tuberlin.tfdacmacs.basics.UnitTestSuite;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.Key;
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
        Key key1 = attributeKeyGenerator.generateAttributeValueKey(globalPublicParameter);
        Key key2 = attributeKeyGenerator.generateAttributeValueKey(globalPublicParameter);

        assertThat(key1.getPublicKey().toBytes()).isNotEqualTo(key2.getPrivateKey().toBytes());
        assertThat(key1.getPublicKey().toBytes()).isNotEqualTo(key2.getPublicKey().toBytes());
    }

}