package de.tuberlin.tfdacmacs.client.certificate.db;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.tuberlin.tfdacmacs.client.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.client.certificate.db.modules.X509Deserializer;
import de.tuberlin.tfdacmacs.client.certificate.db.modules.X509Serializer;
import de.tuberlin.tfdacmacs.client.db.JsonDB;
import org.springframework.stereotype.Component;

import java.security.cert.X509Certificate;

@Component
public class CertificateDB extends JsonDB<Certificate> {
    public CertificateDB() {
        super(Certificate.class);
    }

    @Override
    public Module[] getCustomModule() {
        return new Module[] {
                x509Module()
        };
    }

    public static Module x509Module() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(X509Certificate.class, new X509Serializer());
        module.addDeserializer(X509Certificate.class, new X509Deserializer());
        return module;
    }
}
