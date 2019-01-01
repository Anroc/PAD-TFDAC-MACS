package de.tuberlin.tfdacmacs.centralserver.certificate.data;

import de.tuberlin.tfdacmacs.basics.db.Entity;
import lombok.*;
import org.springframework.lang.Nullable;

import java.security.cert.X509Certificate;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Certificate extends Entity {

    public static final String ROOT_CA = "root";

    @Nullable
    private X509Certificate certificate;

    private String commonName;

    public Certificate(@NonNull String id) {
        super(id);
    }

    public Certificate(@NonNull String id, @NonNull String commonName) {
        super(id);
        this.commonName = commonName;
    }

    public Certificate(@NonNull String id, X509Certificate certificate) {
        super(id);
        this.certificate = certificate;
    }

    public Certificate(String id, X509Certificate certificate, String commonName) {
        this(id, certificate);
        setCommonName(commonName);
    }
}
