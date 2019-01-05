package de.tuberlin.tfdacmacs.centralserver.user;

import de.tuberlin.tfdacmacs.RestTestSuite;
import de.tuberlin.tfdacmacs.lib.certificate.data.dto.CertificateRequest;
import de.tuberlin.tfdacmacs.lib.certificate.data.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import de.tuberlin.tfdacmacs.lib.user.data.DeviceState;
import de.tuberlin.tfdacmacs.lib.user.data.dto.DeviceResponse;
import de.tuberlin.tfdacmacs.lib.user.data.dto.UserResponse;
import de.tuberlin.tfdacmacs.centralserver.authority.data.AttributeAuthority;
import de.tuberlin.tfdacmacs.centralserver.user.data.User;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class UserWorkflowTest extends RestTestSuite {

    private String email = "test@tu-berlin.de";
    private String aid = "aa.tu-berlin.de";
    private String aidCertId = "someCertId";

    @Test
    public void registerDevice() throws IOException, OperatorCreationException {
        AttributeAuthority attributeAuthority = new AttributeAuthority(aid, aidCertId);
        User user = new User(email, aid);
        KeyPair keyPair = new StringAsymmetricCryptEngine().generateKeyPair();

        attributeAuthorityDB.insert(attributeAuthority);
        userDB.insert(user);

        // 0. retrieve root CA
        ResponseEntity<CertificateResponse> rootCa = restTemplate
                .exchange("/certificates/root", HttpMethod.GET, HttpEntity.EMPTY, CertificateResponse.class);
        assertThat(rootCa.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        X509Certificate rootX509Certificate = KeyConverter.from(rootCa.getBody().getCertificate())
                .toX509Certificate();
        certificateUtils.validateCertificate(rootX509Certificate);
        assertThat(certificateUtils.extractCommonName(rootX509Certificate)).isEqualTo("Central Server");

        // 1. user registers certificate
        CertificateRequest certificateRequest = certificateRequestTestFactory.create(email, keyPair);
        ResponseEntity<CertificateResponse> certificateResponseEntity = restTemplate
                .exchange("/certificates", HttpMethod.POST, new HttpEntity(certificateRequest),  CertificateResponse.class);
        assertThat(certificateResponseEntity.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);
        X509Certificate x509Certificate = KeyConverter.from(certificateResponseEntity.getBody().getCertificate())
                .toX509Certificate();
        certificateUtils.validateCertificate(x509Certificate, rootX509Certificate);
        assertThat(certificateUtils.extractCommonName(x509Certificate)).isEqualTo(email);

        // 2. admin pulls users and sees new user
        mutalAuthenticationRestTemplate(AUTHORITY_KEYSTORE);
        ResponseEntity<List<UserResponse>> response = sslRestTemplate
                .exchange("/users", HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<List<UserResponse>>(){});
        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        List<UserResponse> userResponseList = response.getBody();
        assertThat(userResponseList).hasSize(1);
        assertThat(userResponseList.get(0).getId()).isEqualTo(email);
        List<DeviceResponse> devices = userResponseList.get(0).getDevices().stream().collect(Collectors.toList());
        assertThat(devices).hasSize(1);
        assertThat(devices.get(0).getDeviceState()).isEqualByComparingTo(DeviceState.WAITING_FOR_APPROVAL);
        String certificateId = devices.get(0).getCertificateId();

        // 3. admin pulls certificate of device
        ResponseEntity<CertificateResponse> currentCertificate = restTemplate
                .exchange("/certificates/" + certificateId, HttpMethod.GET, HttpEntity.EMPTY, CertificateResponse.class);
        assertThat(currentCertificate.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        x509Certificate = KeyConverter.from(currentCertificate.getBody().getCertificate())
                .toX509Certificate();
        certificateUtils.validateCertificate(x509Certificate, rootX509Certificate);
        assertThat(certificateUtils.extractCommonName(x509Certificate)).isEqualTo(email);
    }
}
