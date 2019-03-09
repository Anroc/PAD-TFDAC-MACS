package de.tuberlin.tfdacmacs.client.register;

import com.google.common.collect.Sets;
import de.tuberlin.tfdacmacs.CommandTestSuite;
import de.tuberlin.tfdacmacs.client.attribute.data.Attribute;
import de.tuberlin.tfdacmacs.client.user.client.dto.DeviceResponse;
import de.tuberlin.tfdacmacs.client.user.client.dto.DeviceState;
import de.tuberlin.tfdacmacs.client.user.client.dto.EncryptedAttributeValueKeyDTO;
import de.tuberlin.tfdacmacs.client.certificate.client.dto.CertificateRequest;
import de.tuberlin.tfdacmacs.client.certificate.client.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.client.register.events.LoginEvent;
import de.tuberlin.tfdacmacs.client.register.events.LogoutEvent;
import de.tuberlin.tfdacmacs.client.register.events.SessionInitializedEvent;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.crypto.rsa.StringSymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.SymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RegisterCommandTest extends CommandTestSuite {

    private X509Certificate currentCertificate;
    private String certificateFingerprint;

    private static final String EMAIL = "some@something.domain";
    private static final SymmetricCryptEngine<String> symmetricCryptEngine = new StringSymmetricCryptEngine();

    @Before
    public void setup() throws CertificateException, CertIOException, OperatorCreationException, BadPaddingException,
            InvalidKeyException, IllegalBlockSizeException {
        KeyPair rootKeyPair = cryptEngine.generateKeyPair();
        X509Certificate rootCertificate = certificateTestFactory.createRootCertificate(rootKeyPair, "CN=Central Server,OU=undo.life,O=tu-berlin,L=Berlin,ST=Berlin,C=DE");

        doAnswer((args) -> {
            CertificateRequest certificateRequest = args.getArgument(0);
            String certRequest = certificateRequest.getCertificateRequest();
            PKCS10CertificationRequest pkcs10CertificationRequest = new PKCS10CertificationRequest(
                            KeyConverter.from(certRequest).toByes());
            currentCertificate = certificateTestFactory.getCertificateSigner().sign(
                            pkcs10CertificationRequest,
                            rootKeyPair.getPrivate(),
                            rootCertificate,
                            EMAIL,
                            KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(
                                    pkcs10CertificationRequest.getSubjectPublicKeyInfo().toASN1Primitive().getEncoded()))
                    );
            certificateFingerprint = certificateUtils.fingerprint(currentCertificate);
            return new CertificateResponse(
                    certificateFingerprint,
                    KeyConverter.from(currentCertificate).toBase64()
            );
        }).when(caClient).postCertificateRequest(any(CertificateRequest.class));

        GlobalPublicParameter globalPublicParameter = gppTestFactory.getGlobalPublicParameter();

        Attribute attribute = new Attribute("authid.attr:value", globalPublicParameter.g1().newRandomElement());
        Key symmetricCipherKey = symmetricCryptEngine.getSymmetricCipherKey();
        EncryptedAttributeValueKeyDTO encryptedAttributeValueKeyDTO = new EncryptedAttributeValueKeyDTO(
                attribute.getId(),
                Base64.encodeBase64String(symmetricCryptEngine.encryptRaw(attribute.getKey().toBytes(), symmetricCipherKey))
        );


        doAnswer(args -> new DeviceResponse(
                args.getArgument(1),
                DeviceState.ACTIVE,
                cryptEngine.encryptRaw(symmetricCipherKey.getEncoded(), keyPairService.getKeyPair(EMAIL).getPublicKey()),
                Sets.newHashSet(encryptedAttributeValueKeyDTO)
        )).when(caClient).getAttributes(eq(EMAIL), anyString());

    }

    @Test
    public void register() {
        testEventListener.recordEvents();
        shell.evaluate(() -> "register " + EMAIL);
        testEventListener.stopRecodingEvents();

        List<SessionInitializedEvent> eventOfType = testEventListener.findEventOfType(SessionInitializedEvent.class);
        assertThat(eventOfType).hasSize(1);
        SessionInitializedEvent sessionInitializedEvent = eventOfType.get(0);
        assertThat(sessionInitializedEvent.getEmail()).isEqualTo(EMAIL);
        assertThat(sessionInitializedEvent.getCertificate().getCertificate()).isEqualTo(currentCertificate);
        assertThat(sessionInitializedEvent.getCertificate().getId()).isEqualTo(certificateFingerprint);
        assertThat(sessionInitializedEvent.getKeyPair()).isNotNull();

        assertThat(keyPairDB.find(EMAIL)).isPresent();
        assertThat(certificateDB.find(EMAIL)).isPresent();

        verify(restTemplateFactory).updateForMutualAuthentication(any(SessionInitializedEvent.class));
    }

    @Test
    public void logout() {
        testEventListener.recordEvents();
        shell.evaluate(() -> "logout");
        testEventListener.stopRecodingEvents();

        List<LogoutEvent> eventOfType = testEventListener.findEventOfType(LogoutEvent.class);
        assertThat(eventOfType).hasSize(1);
    }
    @Test
    public void reset() {
        // depends on
        register();

        testEventListener.recordEvents();
        shell.evaluate(() -> "reset");
        testEventListener.stopRecodingEvents();

        List<LogoutEvent> eventOfType = testEventListener.findEventOfType(LogoutEvent.class);
        assertThat(eventOfType).hasSize(1);
    }

    @Test
    public void login() {
        // depends on
        register();
        logout();

        testEventListener.recordEvents();
        shell.evaluate(() -> "login " + EMAIL);
        testEventListener.stopRecodingEvents();

        List<LoginEvent> eventOfType = testEventListener.findEventOfType(LoginEvent.class);
        assertThat(eventOfType).hasSize(1);
        LoginEvent loginEvent = eventOfType.get(0);

        assertThat(loginEvent.getEmail()).isEqualTo(EMAIL);
        assertThat(loginEvent.getCertificate()).isNotNull();
        assertThat(loginEvent.getKeyPair()).isNotNull();
    }


}
