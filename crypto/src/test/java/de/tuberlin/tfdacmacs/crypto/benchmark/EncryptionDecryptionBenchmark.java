package de.tuberlin.tfdacmacs.crypto.benchmark;

import de.tuberlin.tfdacmacs.crypto.UnitTestSuite;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import org.junit.Before;
import org.junit.Test;

import java.time.temporal.ChronoUnit;
import java.util.List;

public class EncryptionDecryptionBenchmark extends UnitTestSuite {

    private GlobalPublicParameter gpp;

    private final String authorityId = "aa.tu-berlin.de";

    @Before
    public void setup() {
        this.gpp = gppTestFactory.create();
    }

    @Test
    public void encrypt_incrementing_10() {
        SetupWrapper setupWrapper = new SetupWrapper(gpp, authorityId);
        List<AttributeValueKey> attributeValueKeys = setupWrapper.createAttributeValueKeys(1);
        int numberOfRuns = 100;

        for(int numUsers = 1; numUsers <= 250; numUsers += 10) {
            boolean firstRun = numUsers == 1;

            BenchmarkResult rsaRun = Benchmark.rsa()
                    .numberOfRuns(numberOfRuns)
                    .numberOfUsers(numUsers)
                    .configure()
                    .run();

            BenchmarkResult abeRun = Benchmark.abe()
                    .numberOfRuns(numberOfRuns)
                    .numberOfUsers(numUsers)
                    .gpp(gppTestFactory.create())
                    .attributesPerUser(attributeValueKeys)
                    .policy(setupWrapper.policy())
                    .attributeValueKeyProvider(setupWrapper.attributeValueKeyProvider())
                    .authorityKeyProvider(setupWrapper.authorityKeyProvider())
                    .authorityPrivateKey(setupWrapper.authorityPrivateKey())
                    .configure()
                    .run();

            printResults(2, firstRun, numUsers, setupWrapper.createdKeys().size(), rsaRun, abeRun);
        }
    }

    @Test
    public void encrypt_incrementing_10_attribute_increment_1per1User() {
        incrementAttributes(1);
    }

    @Test
    public void encrypt_incrementing_10_attribute_increment_1per2User() {
        incrementAttributes(2);
    }

    @Test
    public void encrypt_incrementing_10_attribute_increment_1per3User() {
        incrementAttributes(3);
    }

    @Test
    public void encrypt_incrementing_10_attribute_increment_1per4User() {
        incrementAttributes(4);
    }

    @Test
    public void encrypt_incrementing_10_attribute_increment_1per5User() {
        incrementAttributes(5);
    }

    private void incrementAttributes(int attributesPerUser) {
        SetupWrapper setupWrapper = new SetupWrapper(gpp, authorityId);
        setupWrapper.createAttributeValueKeys(1);
        int numberOfRuns = 25;
        int stepSize  = 10;

        for(int numUsers = attributesPerUser; numUsers <= 300; numUsers += stepSize ) {
            boolean firstRun = numUsers == attributesPerUser;

            BenchmarkResult rsaRun = Benchmark.rsa()
                    .numberOfRuns(numberOfRuns)
                    .numberOfUsers(numUsers)
                    .configure()
                    .preHeat(firstRun)
                    .run();

            BenchmarkResult abeRun = Benchmark.abe()
                    .numberOfRuns(numberOfRuns)
                    .numberOfUsers(numUsers)
                    .gpp(gppTestFactory.create())
                    .attributesPerUser(setupWrapper.createdKeys())
                    .policy(setupWrapper.policy())
                    .attributeValueKeyProvider(setupWrapper.attributeValueKeyProvider())
                    .authorityKeyProvider(setupWrapper.authorityKeyProvider())
                    .authorityPrivateKey(setupWrapper.authorityPrivateKey())
                    .configure()
                    .preHeat(firstRun)
                    .run();

            printResults(3, firstRun, numUsers, setupWrapper.createdKeys().size(), rsaRun, abeRun);
            setupWrapper.createAttributeValueKeys(stepSize / attributesPerUser);
        }
    }

    private void printResults(int methodIndex, boolean firstRun, int numUsers, int numerOfAttributes, BenchmarkResult rsaRun, BenchmarkResult abeRun) {
        BenchmarkCombinedResult benchmarkCombinedResult = new BenchmarkCombinedResult(rsaRun, abeRun);
        benchmarkCombinedResult.setUnit(ChronoUnit.MILLIS);
        benchmarkCombinedResult.prittyPrintStatistics(numUsers);
        String fileName = Thread.currentThread().getStackTrace()[methodIndex].getMethodName() + ".csv";

        if(firstRun) {
            benchmarkCombinedResult.csvPrintHeaders(fileName);
        }
        benchmarkCombinedResult.csvPrintStatistics(fileName, numUsers, numerOfAttributes);
    }
}
