package de.tuberlin.tfdacmacs.crypto.benchmark;

import org.junit.Test;

public class OrPolicyEncryptionDecryptionBenchmark extends EncryptionDecryptionBenchmark {

    private static final String FILE_DIR = "or-policies";

    public OrPolicyEncryptionDecryptionBenchmark() {
        super(150);
    }

    @Override
    public String getFileDir() {
        return FILE_DIR;
    }

    @Test
    public void encrypt_incrementing_10_attribute_increment_1per1User() {
        incrementAttributes(1, false);
    }

    @Test
    public void encrypt_incrementing_10_attribute_increment_1per2User() {
        incrementAttributes(2, false);
    }

    @Test
    public void encrypt_incrementing_10_attribute_increment_1per4User() {
        incrementAttributes(4, false);
    }

    @Test
    public void encrypt_incrementing_10_attribute_increment_1per8User() {
        incrementAttributes(8, false);
    }

    @Test
    public void encrypt_incrementing_10_attribute_increment_1per16User() {
        incrementAttributes(16, false);
    }

    @Test
    public void encrypt_incrementing_10_attribute_increment_1per140User() {
        setNumUsers(300);
        incrementAttributes(140, false);
    }

    @Test
    public void encrypt_incrementing_10_attribute_increment_1per200User() {
        setNumUsers(420);
        incrementAttributes(200, false);
    }
}
