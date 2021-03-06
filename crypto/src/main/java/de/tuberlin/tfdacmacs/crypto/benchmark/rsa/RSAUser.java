package de.tuberlin.tfdacmacs.crypto.benchmark.rsa;

import de.tuberlin.tfdacmacs.crypto.benchmark.User;
import lombok.Data;

import java.security.PrivateKey;
import java.security.PublicKey;

@Data
public class RSAUser extends User {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

}
