package de.tuberlin.tfdacmacs.crypto.benchmark;

import de.tuberlin.tfdacmacs.crypto.UnitTestSuite;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

public class LeaveBenchmark extends UnitTestSuite {

    protected GlobalPublicParameter gpp;
    protected final String authorityId = "aa.tu-berlin.de";

    @BeforeClass
    public static void init() {
        Locale.setDefault(Locale.US);
    }

    @Before
    public void setup() {
        this.gpp = gppTestFactory.create();
    }

    @Test
    public void leave_attr_1_users_2() {
        leave(1, 2);
    }

    @Test
    public void leave_attr_2_users_2() {
        leave(2, 2);
    }

    @Test
    public void leave_attr_4_users_2() {
        leave(4, 2);
    }

    public void leave(int numAttributes, int numUsers) {
        SetupWrapper setupWrapper = new SetupWrapper(gpp, authorityId);
        List<AttributeValueKey> attributeValueKeys = setupWrapper.createAttributeValueKeys(numAttributes);
        int numberOfRuns = 25;

        assert numUsers >= 2;
        assert numAttributes >= 1;

        int maxCT = 100;

        for(int numCT = 1; numCT <= maxCT; numCT += 1) {
            boolean firstRun = numCT == 1;

            BenchmarkResult rsaRun = Benchmark.rsa()
                    .numberOfRuns(numberOfRuns)
                    .numberOfUsers(numUsers)
                    .numberOfCipherTexts(numCT)
                    .configure()
                    .benchmarkMemberLeave();

            BenchmarkResult abeRun = Benchmark.abe()
                    .numberOfRuns(numberOfRuns)
                    .numberOfUsers(numUsers)
                    .numberOfCipherTexts(numCT)
                    .gpp(gppTestFactory.create())
                    .attributesPerUser(attributeValueKeys)
                    .policy(setupWrapper.policy())
                    .attributeValueKeyProvider(setupWrapper.attributeValueKeyProvider())
                    .authorityKeyProvider(setupWrapper.authorityKeyProvider())
                    .authorityKey(setupWrapper.authorityKey())
                    .configure()
                    .benchmarkMemberLeave();

            printResults(3, firstRun, numCT, numAttributes, rsaRun, abeRun);
        }
    }

    protected void printResults(int methodIndex, boolean firstRun, int numCt, int numerOfAttributes, BenchmarkResult rsaRun, BenchmarkResult abeRun) {
        BenchmarkCombinedResult benchmarkCombinedResult = new BenchmarkCombinedResult(rsaRun, abeRun);
        benchmarkCombinedResult.setUnit(ChronoUnit.MILLIS);
        benchmarkCombinedResult.prittyPrintStatistics(numCt);
        String fileName = "./leave/" + Thread.currentThread().getStackTrace()[methodIndex].getMethodName() + ".csv";

        if(firstRun) {
            benchmarkCombinedResult.csvPrintHeaders(fileName);
        }
        benchmarkCombinedResult.csvPrintStatistics(fileName, numCt, numerOfAttributes);
    }
}

