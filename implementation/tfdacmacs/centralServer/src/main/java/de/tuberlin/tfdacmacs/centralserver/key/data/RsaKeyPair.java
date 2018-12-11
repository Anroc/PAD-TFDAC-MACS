package de.tuberlin.tfdacmacs.centralserver.key.data;

import de.tuberlin.tfdacmacs.basics.db.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@EqualsAndHashCode(callSuper = true)
public class RsaKeyPair extends Entity {

    public static final String ID = "RSA_KEY_PAIR";

    public RsaKeyPair(@NotNull PublicKey publicKey, @NotNull PrivateKey privateKey) {
        super(ID);
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    @NotNull
    private final PublicKey publicKey;
    @NotNull
    private final PrivateKey privateKey;
}
