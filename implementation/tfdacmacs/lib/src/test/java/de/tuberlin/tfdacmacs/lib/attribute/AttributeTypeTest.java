package de.tuberlin.tfdacmacs.lib.attribute;

import de.tuberlin.tfdacmacs.lib.attributes.data.AttributeType;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AttributeTypeTest {

    @Test
    public void matchesType_passes() {
        assertThat(AttributeType.BOOLEAN.matchesType(true)).isTrue();
        assertThat(AttributeType.BOOLEAN.matchesType(false)).isTrue();

        assertThat(AttributeType.STRING.matchesType("asd")).isTrue();
        assertThat(AttributeType.STRING.matchesType("0")).isTrue();

        assertThat(AttributeType.NUMBER.matchesType(0)).isTrue();
        assertThat(AttributeType.NUMBER.matchesType(10000)).isTrue();
    }

    @Test
    public void matchesType_passes_onWrongValue() {
        assertThat(AttributeType.NUMBER.matchesType(0.3f)).isFalse();
        assertThat(AttributeType.NUMBER.matchesType(true)).isFalse();
        assertThat(AttributeType.NUMBER.matchesType("String")).isFalse();

        assertThat(AttributeType.STRING.matchesType(0)).isFalse();
        assertThat(AttributeType.STRING.matchesType(true)).isFalse();

        assertThat(AttributeType.BOOLEAN.matchesType(0)).isFalse();
    }
}
