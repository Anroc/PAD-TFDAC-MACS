package de.tuberlin.tfdacmacs.centralserver.certificate.db;

import de.tuberlin.tfdacmacs.basics.db.CouchbaseDB;
import de.tuberlin.tfdacmacs.centralserver.certificate.data.Certificate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CertificateDB extends CouchbaseDB<Certificate> {

    @Autowired
    public CertificateDB(CertificateRepository repository) {
        super(repository, Certificate.class);
    }

    @Override
    public void drop() {
        getIds().remove(Certificate.ROOT_CA);
        super.drop();
    }
}
