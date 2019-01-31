package de.tuberlin.tfdacmacs.client.twofactor.db;

import de.tuberlin.tfdacmacs.client.db.JsonDB;
import de.tuberlin.tfdacmacs.client.twofactor.data.TwoFactorAuthentication;
import org.springframework.stereotype.Component;

@Component
public class TwoFactorAuthenticationDB extends JsonDB<TwoFactorAuthentication> {
    public TwoFactorAuthenticationDB() {
        super(TwoFactorAuthentication.class);
    }
}
