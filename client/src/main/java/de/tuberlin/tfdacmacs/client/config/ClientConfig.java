package de.tuberlin.tfdacmacs.client.config;

import de.tuberlin.tfdacmacs.client.keypair.config.CertificateKeyStoreConfig;
import de.tuberlin.tfdacmacs.client.keypair.config.GeneralKeyStoreConfig;
import lombok.Data;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;

@Data
@Configuration
@ConfigurationProperties(prefix = "client")
public class ClientConfig {

    @NotBlank
    private String caRootUrl;
    @NotBlank
    private String cspRootUrl;
    @NotBlank
    private String trustStore;
    @NotBlank
    private String trustStorePassword;
    @NestedConfigurationProperty
    private GeneralKeyStoreConfig privateKey;
    @NestedConfigurationProperty
    private CertificateKeyStoreConfig p12Certificate;

    public File locateResource(@NonNull String keyStore) {
        if(keyStore.startsWith("classpath:")) {
            try {
                return ResourceUtils.getFile(keyStore);
            } catch (FileNotFoundException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            return Paths.get(keyStore).toFile();
        }
    }
}
