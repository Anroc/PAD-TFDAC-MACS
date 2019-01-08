package de.tuberlin.tfdacmacs.client.certificate;

import de.tuberlin.tfdacmacs.client.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.client.config.ClientConfig;
import de.tuberlin.tfdacmacs.client.keypair.KeyPairFactory;
import de.tuberlin.tfdacmacs.client.keypair.config.CertificateKeyStoreConfig;
import de.tuberlin.tfdacmacs.client.register.data.dto.CertificateRequest;
import de.tuberlin.tfdacmacs.client.register.data.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.client.rest.CaClient;
import de.tuberlin.tfdacmacs.client.rest.template.RestTemplateFactory;
import de.tuberlin.tfdacmacs.crypto.rsa.certificate.CertificateUtils;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import de.tuberlin.tfdacmacs.crypto.rsa.factory.CertificateRequestFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
@RequiredArgsConstructor
public class CertificateService {

    private final KeyPairFactory keyPairFactory;
    private final CertificateUtils certificateUtils;
    private final CaClient caClient;
    private final CertificateRequestFactory certificateRequestFactory;
    private final ClientConfig clientConfig;

    private final RestTemplateFactory restTemplateFactory;
    private final RestTemplate restTemplate;

    public Certificate certificateRequest(@NonNull String email) {
        try {
            PKCS10CertificationRequest pkcs10CertificationRequest = certificateRequestFactory
                    .create(email, keyPairFactory.getKeyPair());
            CertificateRequest certificateRequest = new CertificateRequest(
                    KeyConverter.from(pkcs10CertificationRequest.getEncoded()).toBase64()
            );

            CertificateResponse certificateResponse = caClient.postCertificateRequest(certificateRequest);
            log.info("received certificate with id [{}] for user [{}]", certificateResponse.getId(), email);

            return new Certificate(
                    certificateResponse.getId(),
                    email,
                    KeyConverter.from(certificateResponse.getCertificate()).toX509Certificate()
            );
        } catch (OperatorCreationException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void generateP12KeyStore(Certificate certificate) {
        String email = certificate.getEmail();

        Path key = toFile("key", certificateUtils.pemFormat(keyPairFactory.getKeyPair().getPrivate()), email);
        Path cert = toFile("crt", certificateUtils.pemFormat(certificate.getCertificate()), email);

        generateP12KeyStore(key, cert, email);

        restTemplateFactory.updateForMutalAuthentication(restTemplate);
    }

    private File generateP12KeyStore(Path key, Path cert, String email) {
        CertificateKeyStoreConfig p12Certificate = clientConfig.getP12Certificate();
        deleteIfExist(clientConfig.getP12Certificate().getLocation());

        cmdExec("openssl pkcs12 -export -clcerts -in " + cert.toString() + " -inkey " + key.toString() +" -out ./" + email + ".p12 -password pass:foobar");
        cmdExec("keytool -importkeystore -destkeystore " + p12Certificate.getLocation() + " -srckeystore ./" + email + ".p12 -srcstorepass foobar -srcstoretype PKCS12 -storepass " + p12Certificate.getKeyStorePassword() + "  -keypass " + p12Certificate.getKeyPassword());
        return clientConfig.locateResource(clientConfig.getP12Certificate().getLocation());
    }

    private void cmdExec(String cmd) {
        Process p;
        try {
            p = Runtime.getRuntime().exec(cmd);
            int term = p.waitFor();

            if(term != 0) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                String line;
                while ((line = reader.readLine())!= null) {
                    System.err.println(line);
                }

                throw new RuntimeException("Exit code was " + term);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while ((line = reader.readLine())!= null) {
                System.out.println(line);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Path toFile(String suffix, String pemFormat, String email) {
        try {
            Path path = deleteIfExist(String.format("./%s.%s", email, suffix));
            Path tempFile = Files.createFile(path);
            Files.write(tempFile, pemFormat.getBytes());
            return tempFile;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path deleteIfExist(String fileLocation) {
        Path path = Paths.get(fileLocation);
        if (path.toFile().exists()) {
            path.toFile().delete();
        }
        return path;
    }
}
