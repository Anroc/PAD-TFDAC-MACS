package de.tuberlin.tfdacmacs.basics.db.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.core.convert.CouchbaseCustomConversions;

import java.util.Arrays;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.couchbase")
public class CouchbaseConfig extends AbstractCouchbaseConfiguration {

    private final List<String> bootstrapHosts;
    private final String bucketName;
    private final String bucketPassword;

    private final CouchbaseElementConverters.Write elementWriter;
    private final CouchbaseElementConverters.Read elementReader;


    @Override
    public CustomConversions customConversions() {
        return new CouchbaseCustomConversions(Arrays.asList(elementReader, elementWriter));
    }
}
