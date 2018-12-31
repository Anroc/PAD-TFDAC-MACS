package de.tuberlin.tfdacmacs.basics.config;

import de.tuberlin.tfdacmacs.basics.crypto.rsa.certificate.JavaKeyStore;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Data
@Configuration
@ConfigurationProperties("server.ssl")
public class KeyStoreConfig {

    @NotBlank
    private String keyStore;
    @NotBlank
    private String keyStorePassword;
    @NotBlank
    private String keyAlias;
    @NotBlank
    private String keyPassword;
    @NotBlank
    private String caAlias;

    @Bean
    public JavaKeyStore javaKeyStore()
            throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException {
        JavaKeyStore javaKeyStore = new JavaKeyStore(keyStore, keyStorePassword);
        if(keyStore.startsWith("classpath:")) {
            javaKeyStore.loadFromClassPath(keyStore.substring(10));
        } else {
            javaKeyStore.loadKeyStore();
        }
        return javaKeyStore;
    }
}
