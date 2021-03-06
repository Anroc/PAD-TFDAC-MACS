package de.tuberlin.tfdacmacs.lib.gpp.data.dto;

import com.couchbase.client.java.repository.annotation.Field;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.security.PublicKey;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RSAPublicKeyDTO {

    @NotBlank
    @Field
    public String publicKey;

    public static RSAPublicKeyDTO from(PublicKey publicKey) {
        return new RSAPublicKeyDTO(KeyConverter.from(publicKey).toBase64());
    }

    public PublicKey toPublicKey() {
        return KeyConverter.from(publicKey).toPublicKey();
    }
}
