package de.tuberlin.tfdacmacs.client.keypair.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyPair {

    @NotNull
    private PrivateKey privateKey;
    @NotNull
    private PublicKey publicKey;

    public static KeyPair from(java.security.KeyPair keyPair) {
        return new KeyPair(keyPair.getPrivate(), keyPair.getPublic());
    }

    public java.security.KeyPair toJavaKeyPair() {
        return new java.security.KeyPair(getPublicKey(), getPrivateKey());
    }
}
