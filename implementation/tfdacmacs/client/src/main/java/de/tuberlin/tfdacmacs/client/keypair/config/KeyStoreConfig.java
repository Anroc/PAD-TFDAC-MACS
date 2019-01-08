package de.tuberlin.tfdacmacs.client.keypair.config;

import de.tuberlin.tfdacmacs.crypto.rsa.certificate.JavaKeyStore;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Data
public abstract class KeyStoreConfig {

    @NotBlank
    private String location;
    @NotBlank
    private String keyPassword;
    @NotBlank
    private String keyStorePassword;

    private String getPlainTextLocation() {
        if(isClasspathResource()) {
            return location.substring(10);
        } else {
            return location;
        }
    }

    private boolean isClasspathResource() {
        return location.startsWith("classpath:");
    }

    public JavaKeyStore toJavaKeyStore() {
        try {
            JavaKeyStore javaKeyStore = new JavaKeyStore(getPlainTextLocation(), keyStorePassword);
            if (isClasspathResource()) {
                javaKeyStore.loadFromClassPath(location);
            } else{
                javaKeyStore.loadKeyStore();
            }
            return javaKeyStore;
        } catch(CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException e){
            throw new RuntimeException(e);
        }
    }
}
