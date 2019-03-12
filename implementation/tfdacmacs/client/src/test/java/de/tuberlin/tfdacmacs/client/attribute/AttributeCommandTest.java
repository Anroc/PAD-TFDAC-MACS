package de.tuberlin.tfdacmacs.client.attribute;

import com.google.common.collect.Sets;
import de.tuberlin.tfdacmacs.CommandTestSuite;
import de.tuberlin.tfdacmacs.client.attribute.data.Attribute;
import de.tuberlin.tfdacmacs.client.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.client.user.client.dto.DeviceResponse;
import de.tuberlin.tfdacmacs.client.user.client.dto.DeviceState;
import de.tuberlin.tfdacmacs.client.user.client.dto.EncryptedAttributeValueKeyDTO;
import de.tuberlin.tfdacmacs.client.user.client.dto.UserResponse;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.UserAttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.rsa.StringSymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.SymmetricCryptEngine;
import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class AttributeCommandTest extends CommandTestSuite {

    private Attribute attribute;

    private final SymmetricCryptEngine<?> symmetricCryptEngine = new StringSymmetricCryptEngine();

    @Before
    public void setup() {
        attribute = new Attribute(
                "testAttribute.type:value",
                new UserAttributeValueKey(gppTestFactory.getGlobalPublicParameter().g1().newRandomElement(), 0L)
        );
    }

    @Test
    public void list() {
        doReturn(Stream.of(attribute)).when(attributeDB).findAll();

        shell.evaluate(() -> "attributes list");

        assertThat(getOutContent()).hasSize(1);
        assertThat(getOutContent().get(0)).contains("testAttribute.type").contains("value");
    }

    @Test
    public void update() throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException {
        String email = "asd@asd.asd";
        Certificate certificate = mock(Certificate.class);
        Key symmetricCipherKey = symmetricCryptEngine.getSymmetricCipherKey();
        EncryptedAttributeValueKeyDTO encryptedAttributeValueKeyDTO = new EncryptedAttributeValueKeyDTO(
                attribute.getId(),
                0L,
                Base64.encodeBase64String(symmetricCryptEngine.encryptRaw(attribute.getUserAttributeValueKey().getKey().toBytes(), symmetricCipherKey))
        );

        doReturn(email).when(session).getEmail();
        doReturn(certificate).when(session).getCertificate();
        doReturn("fingerprint").when(certificate).getId();

        DeviceResponse deviceResponse = new DeviceResponse(
                "fingerprint",
                DeviceState.ACTIVE,
                cryptEngine.encryptRaw(symmetricCipherKey.getEncoded(), keyPairService.getKeyPair(email).getPublicKey()),
                Sets.newHashSet(encryptedAttributeValueKeyDTO)
        );

        doAnswer(args -> new UserResponse(
                email,
                "aa.tu-berlin.de",
                null,
                null,
                Sets.newHashSet(deviceResponse)
        )).when(caClient).getUser(eq(email));
        doReturn(deviceResponse).when(caClient).getAttributes(email, "fingerprint");

        shell.evaluate(() -> "attributes update");

        verify(attributeDB).upsert(eq(attribute.getId()), any(Attribute.class));
    }
}
