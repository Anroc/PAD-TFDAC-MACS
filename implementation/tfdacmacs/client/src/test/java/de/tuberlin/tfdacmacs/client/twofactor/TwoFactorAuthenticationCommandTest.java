package de.tuberlin.tfdacmacs.client.twofactor;

import com.google.common.collect.Sets;
import de.tuberlin.tfdacmacs.CommandTestSuite;
import de.tuberlin.tfdacmacs.client.attribute.client.dto.DeviceResponse;
import de.tuberlin.tfdacmacs.client.attribute.client.dto.DeviceState;
import de.tuberlin.tfdacmacs.client.authority.data.TrustedAuthority;
import de.tuberlin.tfdacmacs.client.authority.events.TrustedAuthorityUpdatedEvent;
import de.tuberlin.tfdacmacs.client.certificate.client.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.client.rest.AAClient;
import de.tuberlin.tfdacmacs.client.twofactor.client.dto.DeviceIdResponse;
import de.tuberlin.tfdacmacs.client.twofactor.client.dto.TwoFactorKeyRequest;
import de.tuberlin.tfdacmacs.client.twofactor.client.dto.TwoFactorKeyResponse;
import de.tuberlin.tfdacmacs.client.twofactor.client.dto.UserResponse;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TwoFactorAuthenticationCommandTest extends CommandTestSuite {

    private final String email = "some.user@tu-berlin.de";
    private final String currentEmail = "test@tu-berlin.de";
    private final String aid = "aa.tu-berlin.de";

    private final String deviceId = UUID.randomUUID().toString();

    @Test
    public void enable() throws CertificateException, CertIOException, OperatorCreationException {
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

        doReturn(currentEmail).when(session).getEmail();
        doReturn(new UserResponse(
                email,
                aid,
                Sets.newHashSet(new DeviceResponse(
                    deviceId, DeviceState.ACTIVE, UUID.randomUUID().toString(), new HashSet<>()
                ))
        )).when(caClient).getUser(email);

        AAClient aaClient = mock(AAClient.class);
        doReturn(aaClient).when(twoFactorAuthenticationClient).getAAClient(aid);
        doReturn(Optional.of(new DeviceIdResponse(deviceId))).when(aaClient).getDevice(email, deviceId);

        X509Certificate deviceCertificate = certificateTestFactory.createRootCertificate();
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

        // TODO: assert capture


    }
}
