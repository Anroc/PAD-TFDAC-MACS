package de.tuberlin.tfdacmacs.client.keypair.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(callSuper = true)
@Configuration
public class GeneralKeyStoreConfig extends KeyStoreConfig {

    @NotBlank
    private String keyAlias;
}
