package de.tuberlin.tfdacmacs.centralserver.key.data;

import com.couchbase.client.java.repository.annotation.Field;
import com.fasterxml.jackson.annotation.JsonIgnore;
import de.tuberlin.tfdacmacs.basics.crypto.rsa.converter.KeyConverter;
import de.tuberlin.tfdacmacs.basics.db.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RsaKeyPair extends Entity {

    public static final String ID = "RSA_KEY_PAIR";

    @Field
    @NotBlank
    private String serializedPublicKey;
    @Field
    @NotBlank
    private String serializedPrivateKey;

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

    @JsonIgnore
    public KeyPair getKeyPair() {
        return new KeyPair(getPublicKey(), getPrivateKey());
    }
}
