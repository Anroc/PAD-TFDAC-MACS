package de.tuberlin.tfdacmacs.centralserver.certificate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties("central-server.certificate")
public class CertificateConfig {

    @NotBlank
    private List<String> ips;
    @NotBlank
    private List<String> domains;
    @Min(1)
    private long validForDays = 365L;
}
