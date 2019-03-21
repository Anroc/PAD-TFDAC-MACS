package de.tuberlin.tfdacmacs.crypto.benchmark;

import java.util.List;

public interface CryptoSystem<T extends CrytoAlgorithm> {

    void encrypt(byte[] plainText, T algorithm, List<User> users);

    void decrypt(byte[] cipherText, T algorithm, List<User> users);
}
