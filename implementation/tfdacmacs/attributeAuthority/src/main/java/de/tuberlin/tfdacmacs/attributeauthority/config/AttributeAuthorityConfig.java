package de.tuberlin.tfdacmacs.attributeauthority.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "attribute-authority")
public class AttributeAuthorityConfig {

    @NotBlank
    private String id;

    private boolean requestCaOnInit = true;

    @NotBlank
    private String caRootUrl;
}
