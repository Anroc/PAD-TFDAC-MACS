package de.tuberlin.tfdacmacs.basics.crypto.pairing;

import de.tuberlin.tfdacmacs.basics.UnitTestSuite;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.AttributeValueKey;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AttributeKeyManagerTest extends UnitTestSuite {

    private GlobalPublicParameter globalPublicParameter;
    private AttributeKeyManager attributeKeyManager;

    @Before
    public void setup() {
        attributeKeyManager = new AttributeKeyManager(hashGenerator);
        globalPublicParameter = gppTestFactory.create();
    }

    @Test
    public void generateAttributeKeys_passes_forTwoValuesGeneration() {
        AttributeValueKey key1 = attributeKeyManager.generateAttributeValueKey(globalPublicParameter, "asd");
        AttributeValueKey key2 = attributeKeyManager.generateAttributeValueKey(globalPublicParameter, "dsa");

        assertThat(key1.getPublicKey().getKey().toBytes()).isNotEqualTo(key2.getPrivateKey().getKey().toBytes());
        assertThat(key1.getPublicKey().getKey().toBytes()).isNotEqualTo(key2.getPublicKey().getKey().toBytes());
    }

}