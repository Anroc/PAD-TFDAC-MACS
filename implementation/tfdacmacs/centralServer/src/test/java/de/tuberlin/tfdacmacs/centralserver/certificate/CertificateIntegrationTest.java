package de.tuberlin.tfdacmacs.centralserver.certificate;

import de.tuberlin.tfdacmacs.IntegrationTestSuite;
import de.tuberlin.tfdacmacs.lib.certificate.data.dto.CertificateRequest;
import de.tuberlin.tfdacmacs.lib.certificate.data.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import de.tuberlin.tfdacmacs.centralserver.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.centralserver.user.data.User;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.openssl.jcajce.JcaMiscPEMGenerator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.io.pem.PemObjectGenerator;
import org.bouncycastle.util.io.pem.PemWriter;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class CertificateIntegrationTest extends IntegrationTestSuite {

    private String email = "test@tu-berlin.de";
    private KeyPair clientKeys = new StringAsymmetricCryptEngine(4096).generateKeyPair();

    @Test
    public void getRootCA() {
        ResponseEntity<CertificateResponse> userCreationResponseResponseEntity = restTemplate
                .exchange("/certificates/root", HttpMethod.GET, HttpEntity.EMPTY, CertificateResponse.class);
        assertThat(userCreationResponseResponseEntity.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        CertificateResponse body = userCreationResponseResponseEntity.getBody();
        String id = body.getId();
        assertThat(id).isNotBlank().isEqualTo(Certificate.ROOT_CA);
        assertThat(body.getCertificate()).isNotNull();
        assertThat(certificateDB.exist(id)).isTrue();
    }

    @Test
    public void signingRequest()
            throws IOException, OperatorCreationException, CertificateException, NoSuchProviderException {
        User user = new User(email, "aa.tu-berlin.de");
        userDB.insert(user);


        CertificateRequest certificateRequest = certificateRequestTestFactory.create(email, clientKeys);
        ResponseEntity<CertificateResponse> certificateResponseEntity = restTemplate
                .exchange("/certificates", HttpMethod.POST, new HttpEntity(certificateRequest),  CertificateResponse.class);

        assertThat(certificateResponseEntity.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);
        CertificateResponse body = certificateResponseEntity.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCertificate()).isNotNull();

        X509Certificate x509Certificate =
                (X509Certificate) CertificateFactory.getInstance("X.509", "BC")
                        .generateCertificate(new ByteArrayInputStream(
                                KeyConverter.from(body.getCertificate()).toByes()));
        assertThat(x509Certificate.getType()).isEqualTo("X.509");
        assertThat(((X509Principal) x509Certificate.getIssuerDN()).getValues()).contains(
                "Central Server",
                "undo.life",
                "tu-berlin",
                "Berlin",
                "DE"
        );
        assertThat(((X509Principal) x509Certificate.getSubjectDN()).getValues()).contains(
                email,
                "undo.life",
                "tu-berlin",
                "Berlin",
                "DE"
        );

        printPEMFormat(x509Certificate);
        printPEMFormat(clientKeys.getPrivate());
    }

    private void printPEMFormat(Object o) throws IOException {
        StringWriter sw = new StringWriter();
        try (PemWriter pw = new PemWriter(sw)) {
            PemObjectGenerator gen = new JcaMiscPEMGenerator(o);
            pw.writeObject(gen);
        }
        System.out.println(sw.toString());
    }
}
