package de.tuberlin.tfdacmacs.basics.db.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.core.convert.CouchbaseCustomConversions;
import org.springframework.data.couchbase.repository.auditing.EnableCouchbaseAuditing;

import javax.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Configuration
@EnableCouchbaseAuditing
public class CouchbaseConfig extends AbstractCouchbaseConfiguration {

    @NotBlank
    @Value("${spring.couchbase.bootstrap-hosts}")
    private List<String> bootstrapHosts;

    @NotBlank
    @Value("${spring.couchbase.bucket.name}")
    private String bucketName;

    @NotBlank
    @Value("${spring.couchbase.bucket.password}")
    private String bucketPassword;

    private final CouchbaseElementConverters.Write elementWriter;
    private final CouchbaseElementConverters.Read elementReader;

    private final CouchbaseX509CertificateConverter.Write x509CertificateWriter;
    private final CouchbaseX509CertificateConverter.Read x509CertificateReader;


    @Override
    public CustomConversions customConversions() {
        return new CouchbaseCustomConversions(
                Arrays.asList(
                        elementReader, elementWriter,
                        x509CertificateReader, x509CertificateWriter
                ));
    }
}
