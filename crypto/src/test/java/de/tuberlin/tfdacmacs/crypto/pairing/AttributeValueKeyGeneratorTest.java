package de.tuberlin.tfdacmacs.crypto.pairing;

import de.tuberlin.tfdacmacs.crypto.UnitTestSuite;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AttributeValueKeyGeneratorTest extends UnitTestSuite {

    private GlobalPublicParameter globalPublicParameter;
    private AttributeValueKeyGenerator attributeValueKeyGenerator;

    @Before
    public void setup() {
        attributeValueKeyGenerator = new AttributeValueKeyGenerator(hashGenerator);
        globalPublicParameter = gppTestFactory.create();
    }

    @Test
    public void generateAttributeKeys_passes_forTwoValuesGeneration() {
        AttributeValueKey key1 = attributeValueKeyGenerator.generateNew(globalPublicParameter, "asd");
        AttributeValueKey key2 = attributeValueKeyGenerator.generateNew(globalPublicParameter, "dsa");

        assertThat(key1.getPublicKey().getKey().toBytes()).isNotEqualTo(key2.getPrivateKey().getKey().toBytes());
        assertThat(key1.getPublicKey().getKey().toBytes()).isNotEqualTo(key2.getPublicKey().getKey().toBytes());
    }

}