package de.tuberlin.tfdacmacs.client.certificate.db;

import com.fasterxml.jackson.databind.Module;
import de.tuberlin.tfdacmacs.client.certificate.data.Certificate;
import de.tuberlin.tfdacmacs.client.db.JsonDB;
import org.springframework.stereotype.Component;

import static de.tuberlin.tfdacmacs.client.db.ModelFactory.x509Module;

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


}
