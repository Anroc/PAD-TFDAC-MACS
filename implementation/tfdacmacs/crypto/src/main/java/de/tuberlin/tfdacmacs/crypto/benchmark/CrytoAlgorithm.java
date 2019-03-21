package de.tuberlin.tfdacmacs.crypto.benchmark;

public interface CrytoAlgorithm {

    void setup(Object... varArgs);

    byte[] encrypt(byte[] plainText, Object... varArgs);

    byte[] decrypt(byte[] cipherText, Object... varArgs);

    default <T> T extract(Object[] objects, int pos) {
        return (T) objects[pos];
    }
}
