package de.tuberlin.tfdacmacs.attributeauthority.security.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "attribute-authority.credentials")
public class CredentialConfig {

    @NotBlank
    private String username;
    @NotNull
    private String password;
}
