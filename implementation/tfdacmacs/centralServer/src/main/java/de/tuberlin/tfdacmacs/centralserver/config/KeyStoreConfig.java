package de.tuberlin.tfdacmacs.centralserver.config;

import de.tuberlin.tfdacmacs.centralserver.certificates.JavaKeyStore;
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
        javaKeyStore.loadKeyStore();
        return javaKeyStore;
    }
}
