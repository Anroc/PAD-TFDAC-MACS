package de.tuberlin.tfdacmacs.centralserver.key.data;

import com.couchbase.client.java.repository.annotation.Field;
import com.fasterxml.jackson.annotation.JsonIgnore;
import de.tuberlin.tfdacmacs.basics.crypto.rsa.converter.KeyConverter;
import de.tuberlin.tfdacmacs.basics.db.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@EqualsAndHashCode(callSuper = true)
public class RsaKeyPair extends Entity {

    public static final String ID = "RSA_KEY_PAIR";

    @Field
    @NotBlank
    private final String serializedPublicKey;
    @Field
    @NotBlank
    private final String serializedPrivateKey;


    public RsaKeyPair(@NotNull PublicKey publicKey, @NotNull PrivateKey privateKey) {
        super(ID);
        this.serializedPrivateKey = KeyConverter.from(privateKey).toBase64();
        this.serializedPublicKey = KeyConverter.from(publicKey).toBase64();
    }


    @JsonIgnore
    public PublicKey getPublicKey() {
        return KeyConverter.from(serializedPublicKey).toPublicKey();
    }

    @JsonIgnore
    public PrivateKey getPrivateKey() {
        return KeyConverter.from(serializedPrivateKey).toPrivateKey();
    }

}
