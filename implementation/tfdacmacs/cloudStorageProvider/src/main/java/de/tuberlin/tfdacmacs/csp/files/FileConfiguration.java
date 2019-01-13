package de.tuberlin.tfdacmacs.csp.files;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotBlank;
import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@ConfigurationProperties("csp.files")
public class FileConfiguration {

    @NotBlank
    private String dataDir;

    @PostConstruct
    public void init() {
        Path path = Paths.get(dataDir);
        path.toFile().mkdirs();
    }
}
