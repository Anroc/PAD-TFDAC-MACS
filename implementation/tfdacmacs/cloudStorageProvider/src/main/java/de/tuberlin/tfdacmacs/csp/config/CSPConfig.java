package de.tuberlin.tfdacmacs.csp.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@ConfigurationProperties("csp")
public class CSPConfig {

    @NotBlank
    private String caRootUrl;

    private boolean requestCaOnInit = true;
}
