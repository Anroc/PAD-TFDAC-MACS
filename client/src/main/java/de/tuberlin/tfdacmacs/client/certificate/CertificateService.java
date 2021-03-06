package de.tuberlin.tfdacmacs.client.certificate;

import de.tuberlin.tfdacmacs.client.certificate.client.CertificateClient;
import de.tuberlin.tfdacmacs.client.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.client.certificate.db.CertificateDB;
import de.tuberlin.tfdacmacs.client.config.ClientConfig;
import de.tuberlin.tfdacmacs.client.keypair.KeyPairService;
import de.tuberlin.tfdacmacs.client.keypair.config.CertificateKeyStoreConfig;
import de.tuberlin.tfdacmacs.crypto.rsa.certificate.CertificateUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
@RequiredArgsConstructor
public class CertificateService {

    private final KeyPairService keyPairService;
    private final CertificateUtils certificateUtils;

    private final ClientConfig clientConfig;
    private final CertificateDB certificateDB;

    private final CertificateClient certificateClient;

    public Certificate login(@NonNull String email) {
        File file = getP12KeyStore(email);
        if(file.exists()) {
            return certificateDB.find(email).orElseThrow(
                    () -> new IllegalStateException(
                            String.format("P12 Certificate exist but certificate object in DB does not [%s]", email))
            );
        } else {
            throw new IllegalStateException(
                    String.format("Can not login. No state object is present.")
            );
        }
    }

    private File getP12KeyStore(@NonNull String email) {
        String location = clientConfig.getP12Certificate().getLocation();
        if(! location.endsWith(File.separator)) {
            location += File.separator;
        }
        File file = clientConfig.locateResource(location + email + ".jks");
        file.mkdirs();
        return file;
    }



    public void generateP12KeyStore(Certificate certificate) {
        String email = certificate.getEmail();

        Path key = toFile("key", certificateUtils.pemFormat(keyPairService.getKeyPair(email).getPrivateKey()), email);
        Path cert = toFile("crt", certificateUtils.pemFormat(certificate.getCertificate()), email);

        generateP12KeyStore(key, cert, email);
    }

    private File generateP12KeyStore(Path key, Path cert, String email) {
        CertificateKeyStoreConfig p12Certificate = clientConfig.getP12Certificate();
        deleteIfExist(getP12KeyStore(email));

        cmdExec("openssl pkcs12 -export -clcerts -in " + cert.toString() + " -inkey " + key.toString() +" -out ./" + email + ".p12 -password pass:foobar");
        cmdExec("keytool -importkeystore -destkeystore " + p12Certificate.getLocation() + email + ".jks" + " -srckeystore ./" + email + ".p12 -srcstorepass foobar -srcstoretype PKCS12 -storepass " + p12Certificate.getKeyStorePassword() + "  -keypass " + p12Certificate.getKeyPassword());
        return getP12KeyStore(email);
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
        return deleteIfExist(path.toFile());
    }

    private Path deleteIfExist(File file) {
        if(file.exists()) {
            file.delete();
        }
        return file.toPath();
    }

    public Certificate certificateRequest(String email) {
        return certificateClient.certificateRequest(email);
    }
}
