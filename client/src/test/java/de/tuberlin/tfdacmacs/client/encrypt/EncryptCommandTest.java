package de.tuberlin.tfdacmacs.client.encrypt;

import de.tuberlin.tfdacmacs.CommandTestSuite;
import de.tuberlin.tfdacmacs.client.attribute.data.dto.PublicAttributeValueResponse;
import de.tuberlin.tfdacmacs.client.authority.data.TrustedAuthority;
import de.tuberlin.tfdacmacs.client.authority.data.dto.AttributeAuthorityResponse;
import de.tuberlin.tfdacmacs.client.authority.events.TrustedAuthorityUpdatedEvent;
import de.tuberlin.tfdacmacs.client.authority.exception.NotTrustedAuthorityException;
import de.tuberlin.tfdacmacs.client.csp.data.dto.CipherTextDTO;
import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EncryptCommandTest extends CommandTestSuite {

    private static final String FILE_NAME = "test.file";

    @Autowired
    private EncryptCommand encryptCommand;

    private File file;
    private String authorityId = "aa.tu-berlin.de";
    private String attrId1 = String.format("%s.role:Student", authorityId);
    private String attrId2 = String.format("%s.role:Professor", authorityId);
    private String policy = String.format("(%s or %s)", attrId1, attrId2);

    @Before
    public void setupTestFile() throws IOException {
        file = Files.write(Paths.get(FILE_NAME), "helloWorld".getBytes()).toFile();
    }

    @After
    public void cleanUp() {
        file.delete();
    }

    @Test
    public void encrypt_without2FA() {
        doReturn(true)
                .when(stringAsymmetricCryptEngine).isSignatureAuthentic(anyString(), anyString(), any(PublicKey.class));
        X509Certificate certificate = mock(X509Certificate.class);
        doReturn(mock(PublicKey.class)).when(certificate).getPublicKey();
        signatureVerifier.updateTrustedPublicKeys(
                new TrustedAuthorityUpdatedEvent(
                        new TrustedAuthority(authorityId, UUID.randomUUID().toString(), certificate)
                )
        );
        doReturn(new PublicAttributeValueResponse(
                ElementConverter.convert(randomElementG1()),
                "Student",
                "signature"
        )).when(caClient).getAttributeValue(attrId1.split(":")[0], attrId1.split(":")[1]);
        doReturn(new PublicAttributeValueResponse(
                ElementConverter.convert(randomElementG1()),
                "Professor",
                "signature"
        )).when(caClient).getAttributeValue(attrId2.split(":")[0], attrId2.split(":")[1]);
        doReturn(new AttributeAuthorityResponse(
                authorityId,
                "SomeCertId",
                ElementConverter.convert(randomElementG1()),
                "signature"
        )).when(caClient).getAuthority(authorityId);

        doNothing().when(cspClient).createCipherText(any(CipherTextDTO.class));
        doReturn(cspClient).when(cspClient).withHeaders(any(HttpHeaders.class));
        doNothing().when(cspClient).createFile(anyString(), any(MultiValueMap.class));

        encryptCommand.encrypt(FILE_NAME, null, policy);

        verify(cspClient, times(2)).createCipherText(any(CipherTextDTO.class));
        verify(cspClient, times(1)).createFile(anyString(), any(MultiValueMap.class));
    }

    @Test
    public void encrypt_fails_onUntrustedAuthority() {
        String hpiAuthorityId = "aa.hpi.de";
        String hpiAttrId = "aa.hpi.de.role:Student";

        doReturn(new PublicAttributeValueResponse(
                ElementConverter.convert(randomElementG1()),
                "Student",
                "signature"
        )).when(caClient).getAttributeValue(hpiAttrId.split(":")[0], hpiAttrId.split(":")[1]);
        doReturn(new AttributeAuthorityResponse(
                hpiAuthorityId,
                "SomeCertId",
                ElementConverter.convert(randomElementG1()),
                "signature"
        )).when(caClient).getAuthority(hpiAuthorityId);
        doReturn(true)
                .when(stringAsymmetricCryptEngine).isSignatureAuthentic(anyString(), anyString(), any(PublicKey.class));

        Assertions.assertThatExceptionOfType(NotTrustedAuthorityException.class).isThrownBy(
                () -> encryptCommand.encrypt(FILE_NAME, null, hpiAttrId)
        );

        verify(cspClient, times(0)).createCipherText(any(CipherTextDTO.class));
        verify(cspClient, times(0)).createFile(anyString(), any(MultiValueMap.class));
    }
}
