package de.tuberlin.tfdacmacs.client.decrypt;

import de.tuberlin.tfdacmacs.CommandTestSuite;
import de.tuberlin.tfdacmacs.client.attribute.data.Attribute;
import de.tuberlin.tfdacmacs.client.csp.client.dto.CipherTextDTO;
import org.assertj.core.util.Lists;
import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

public class DecryptCommandTest extends CommandTestSuite {

    @Test
    public void check() {
        String attributeId = "aa.tu-berlin.de.role:student";
        String ctId = UUID.randomUUID().toString();
        attributeDB.upsert(attributeId, new Attribute(attributeId, 0L, randomElementG1()));

        doReturn(
                Lists.newArrayList(CipherTextDTO.from(cipherTextTestFacotry.create(ctId, null, attributeId)))
        ).when(caClient).getCipherTexts(Lists.newArrayList(attributeId));

        evaluate("check");

        assertThat(containsSubSequence(getOutContent(), attributeId)).isTrue();
    }
}
