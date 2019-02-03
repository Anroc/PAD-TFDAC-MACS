package de.tuberlin.tfdacmacs.client.authority.db;

import com.fasterxml.jackson.databind.Module;
import de.tuberlin.tfdacmacs.client.authority.data.TrustedAuthority;
import de.tuberlin.tfdacmacs.client.db.JsonDB;
import org.springframework.stereotype.Component;

import static de.tuberlin.tfdacmacs.client.db.ModelFactory.x509Module;

@Component
public class TrustedAuthorityDB extends JsonDB<TrustedAuthority> {
    public TrustedAuthorityDB() {
        super(TrustedAuthority.class);
    }

    @Override public Module[] getCustomModule() {
        return new Module[] {
                x509Module()
        };
    }
}
