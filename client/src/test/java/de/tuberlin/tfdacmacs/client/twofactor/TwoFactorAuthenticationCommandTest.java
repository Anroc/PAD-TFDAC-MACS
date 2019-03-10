package de.tuberlin.tfdacmacs.client.twofactor;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.tuberlin.tfdacmacs.CommandTestSuite;
import de.tuberlin.tfdacmacs.client.user.client.dto.DeviceResponse;
import de.tuberlin.tfdacmacs.client.user.client.dto.DeviceState;
import de.tuberlin.tfdacmacs.client.attribute.client.dto.PublicAttributeValueResponse;
import de.tuberlin.tfdacmacs.client.authority.data.TrustedAuthority;
import de.tuberlin.tfdacmacs.client.authority.events.TrustedAuthorityUpdatedEvent;
import de.tuberlin.tfdacmacs.client.certificate.client.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.client.csp.client.dto.CipherTextDTO;
import de.tuberlin.tfdacmacs.client.csp.client.dto.TwoFactorCipherTextUpdateRequest;
import de.tuberlin.tfdacmacs.client.keypair.data.KeyPair;
import de.tuberlin.tfdacmacs.client.rest.AAClient;
import de.tuberlin.tfdacmacs.client.twofactor.client.dto.*;
import de.tuberlin.tfdacmacs.client.twofactor.data.TwoFactorAuthentication;
import de.tuberlin.tfdacmacs.client.user.client.dto.TwoFactorPublicKeyDTO;
import de.tuberlin.tfdacmacs.client.user.client.dto.UserResponse;
import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorKey;
import de.tuberlin.tfdacmacs.crypto.pairing.util.AttributeValueId;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TwoFactorAuthenticationCommandTest extends CommandTestSuite {

    private final String email = "some.user@tu-berlin.de";
    private final String currentEmail = "test@tu-berlin.de";
    private final String aid = "aa.tu-berlin.de";

    private String deviceId;
    private X509Certificate deviceCertificate;

    @Before
    public void setup() throws CertificateException, CertIOException, OperatorCreationException {
        deviceCertificate = certificateTestFactory.createRootCertificate();
        deviceId = certificateUtils.fingerprint(deviceCertificate);
    }

    @Test
    public void trust() {
        X509Certificate certificate = mock(X509Certificate.class);
        doReturn(mock(PublicKey.class)).when(certificate).getPublicKey();
        semanticValidator.updateTrustedPublicKeys(
                new TrustedAuthorityUpdatedEvent(
                        new TrustedAuthority(
                                aid,
                                UUID.randomUUID().toString(),
                                certificate
                        )
                )
        );

        doReturn(new UserResponse()).when(caClient).updateTwoFactorPublicKey(eq(currentEmail), any(
                TwoFactorPublicKeyDTO.class));
        doReturn(currentEmail).when(session).getEmail();
        doReturn(KeyPair.from(stringAsymmetricCryptEngine.getAsymmetricCipherKeyPair())).when(session).getKeyPair();
        doReturn(new UserResponse(
                email,
                aid,
                null,
                null,
                Sets.newHashSet(new DeviceResponse(
                    deviceId, DeviceState.ACTIVE, UUID.randomUUID().toString(), new HashSet<>()
                ))
        )).when(caClient).getUser(email);

        AAClient aaClient = mock(AAClient.class);
        doReturn(aaClient).when(twoFactorAuthenticationClient).getAAClient(aid);
        doReturn(Optional.of(new DeviceIdResponse(deviceId))).when(aaClient).getDevice(email, deviceId);

        doReturn(new CertificateResponse(
                certificateUtils.fingerprint(deviceCertificate),
                KeyConverter.from(deviceCertificate).toBase64()
        )).when(caClient).getCertificate(deviceId);

        doReturn(
                new TwoFactorKeyResponse()
        ).when(caClient).createTwoFactorKey(any(TwoFactorKeyRequest.class));

        ArgumentCaptor<TwoFactorKeyRequest> captor = ArgumentCaptor.forClass(TwoFactorKeyRequest.class);
        evaluate("2fa trust " + email);

        verify(caClient).createTwoFactorKey(captor.capture());
        verify(caClient, times(1)).updateTwoFactorPublicKey(eq(currentEmail), any(TwoFactorPublicKeyDTO.class));
        TwoFactorKeyRequest value = captor.getValue();
        assertThat(value.getUserId()).isEqualTo(email);
        assertThat(value.getEncryptedTwoFactorKeys())
                .containsOnlyKeys(deviceId);
    }

    @Test
    public void distrust() {
        TwoFactorKey twoFactorKey = new TwoFactorKey(
                randomElementZr(),
                0L
        );
        twoFactorKey.putPublicKey(email, randomElementG1());
        twoFactorKey.putPublicKey(currentEmail, randomElementG1());

        TwoFactorAuthentication twoFactorAuthentication = new TwoFactorAuthentication(currentEmail, twoFactorKey);
        twoFactorAuthenticationDB.insert(twoFactorAuthentication.getOwnerId(), twoFactorAuthentication);

        AttributeValueId attributeValueId = new AttributeValueId("aa.tu-berlin.de.role:student");

        TwoFactorKeyResponse twoFactorKeyResponse = new TwoFactorKeyResponse(
                UUID.randomUUID().toString(),
                email,
                currentEmail,
                new HashMap<>(),
                new ArrayList<>()
        );

        TwoFactorKeyResponse twoFactorKeyResponseSelf = new TwoFactorKeyResponse(
                UUID.randomUUID().toString(),
                currentEmail,
                currentEmail,
                new HashMap<>(),
                new ArrayList<>()
        );

        ArrayList<Object> ctList = Lists.newArrayList(
                CipherTextDTO.from(cipherTextTestFacotry
                        .create(UUID.randomUUID().toString(), currentEmail, attributeValueId.getAttributeValueId())));

        doReturn(new UserResponse()).when(caClient).updateTwoFactorPublicKey(eq(currentEmail), any(TwoFactorPublicKeyDTO.class));
        doReturn(currentEmail).when(session).getEmail();
        doReturn(KeyPair.from(stringAsymmetricCryptEngine.getAsymmetricCipherKeyPair())).when(session).getKeyPair();
        doReturn(ctList).when(caClient).getCipherTexts(currentEmail);
        doReturn(new PublicAttributeValueResponse(
                ElementConverter.convert(randomElementG1()),
                "student",
                "someSignature",
                0L
        )).when(caClient).getAttributeValue(attributeValueId.getAttributeId(), attributeValueId.getValue());

        X509Certificate certificate = mock(X509Certificate.class);
        PublicKey pubKey = mock(PublicKey.class);
        doReturn(pubKey).when(certificate).getPublicKey();
        semanticValidator.updateTrustedPublicKeys(
                new TrustedAuthorityUpdatedEvent(
                        new TrustedAuthority(
                                aid,
                                UUID.randomUUID().toString(),
                                certificate
                        )
                )
        );
        doReturn(true).when(stringAsymmetricCryptEngine)
                .isSignatureAuthentic(anyString(), anyString(), any(PublicKey.class));
        doReturn(
                Lists.newArrayList(twoFactorKeyResponse, twoFactorKeyResponseSelf)
        ).when(caClient).getTwoFactorKeys();
        doReturn(twoFactorKeyResponse).when(caClient)
                .putTwoFactorUpdateKey(
                        eq(twoFactorKeyResponse.getId()),
                        any(TwoFactorUpdateKeyRequest.class)
                );
        doReturn(ctList).when(caClient).putCipherTextUpdates2FA(any(TwoFactorCipherTextUpdateRequest.class));
        doNothing().when(caClient).deleteTwoFactorKey(twoFactorKeyResponse.getId());

        evaluate("2fa distrust " + email);

        verify(caClient, times(1)).updateTwoFactorPublicKey(eq(currentEmail), any(TwoFactorPublicKeyDTO.class));
        verify(caClient, times(1)).putCipherTextUpdates2FA(any(TwoFactorCipherTextUpdateRequest.class));
        verify(caClient, times(1)).putTwoFactorUpdateKey(eq(twoFactorKeyResponseSelf.getId()),
                any(TwoFactorUpdateKeyRequest.class));
        verify(caClient, times(1)).deleteTwoFactorKey(twoFactorKeyResponse.getId());

    }
}
