package de.tuberlin.tfdacmacs.crypto.benchmark;

import de.tuberlin.tfdacmacs.crypto.UnitTestSuite;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import org.junit.Before;
import org.junit.Test;

import java.time.temporal.ChronoUnit;

public class EncryptionDecryptionBenchmark extends UnitTestSuite {

    private GlobalPublicParameter gpp;

    private final String authorityId = "aa.tu-berlin.de";

    @Before
    public void setup() {
        this.gpp = gppTestFactory.create();
    }

    @Test
    public void encrypt_decrypt_incremeanting_100() {
        SetupWrapper setupWrapper = new SetupWrapper(gpp, authorityId);
        String rsaFile = "rsaFile.csv";
        String abeFile = "abeFile.csv";

        for(int numUsers = 1; numUsers <= 500; numUsers += 10) {
            BenchmarkResult rsaRun = Benchmark.rsa()
                    .numberOfRuns(50)
                    .numberOfUsers(numUsers)
                    .configure()
                    .run();

            printResults(numUsers, rsaRun, rsaFile);

            BenchmarkResult abeRun = Benchmark.abe()
                    .numberOfRuns(50)
                    .numberOfUsers(numUsers)
                    .gpp(gppTestFactory.create())
                    .attributesPerUser(setupWrapper.createAttributeValueKeys(2))
                    .policy(setupWrapper.policy())
                    .attributeValueKeyProvider(setupWrapper.attributeValueKeyProvider())
                    .authorityKeyProvider(setupWrapper.authorityKeyProvider())
                    .authorityPrivateKey(setupWrapper.authorityPrivateKey())
                    .configure()
                    .run();

            printResults(numUsers, abeRun, abeFile);
        }
    }

    private void printResults(int numUsers, BenchmarkResult run, String fileName) {
        run.setUnit(ChronoUnit.MILLIS);
        run.prittyPrintStatistics(numUsers);
        if(numUsers <= 1) {
            run.csvPrintHeaders(fileName);
        }
        run.csvPrintStatistics(fileName, numUsers);
    }
}
