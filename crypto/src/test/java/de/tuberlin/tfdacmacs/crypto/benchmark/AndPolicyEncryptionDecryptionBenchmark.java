package de.tuberlin.tfdacmacs.crypto.benchmark;

import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import org.junit.Test;

import java.util.List;

public class AndPolicyEncryptionDecryptionBenchmark extends EncryptionDecryptionBenchmark {

    private static final String FILE_DIR = "and-policies";

    @Override
    public String getFileDir() {
        return FILE_DIR;
    }

    @Test
    public void encrypt_incrementing_10() {
        SetupWrapper setupWrapper = new SetupWrapper(gpp, authorityId);
        List<AttributeValueKey> attributeValueKeys = setupWrapper.createAttributeValueKeys(1);
        int numberOfRuns = 25;

        for(int numUsers = 1; numUsers <= getNumUsers(); numUsers += 10) {
            boolean firstRun = numUsers == 1;

            BenchmarkResult rsaRun = Benchmark.rsa()
                    .numberOfRuns(numberOfRuns)
                    .numberOfUsers(numUsers)
                    .configure()
                    .benchmarkEncrypt();

            BenchmarkResult abeRun = Benchmark.abe()
                    .numberOfRuns(numberOfRuns)
                    .numberOfUsers(numUsers)
                    .gpp(gppTestFactory.create())
                    .attributesPerUser(attributeValueKeys)
                    .policy(setupWrapper.policy())
                    .attributeValueKeyProvider(setupWrapper.attributeValueKeyProvider())
                    .authorityKeyProvider(setupWrapper.authorityKeyProvider())
                    .authorityKey(setupWrapper.authorityKey())
                    .configure()
                    .benchmarkEncrypt();

            printResults(2, firstRun, numUsers, setupWrapper.createdKeys().size(), rsaRun, abeRun);
        }
    }

    @Test
    public void encrypt_incrementing_10_attribute_increment_1per1User() {
        incrementAttributes(1, true);
    }

    @Test
    public void encrypt_incrementing_10_attribute_increment_1per2User() {
        incrementAttributes(2, true);
    }

    @Test
    public void encrypt_incrementing_10_attribute_increment_1per3User() {
        incrementAttributes(3, true);
    }

    @Test
    public void encrypt_incrementing_10_attribute_increment_1per4User() {
        incrementAttributes(4, true);
    }

    @Test
    public void encrypt_incrementing_10_attribute_increment_1per5User() {
        incrementAttributes(5, true);
    }
}
