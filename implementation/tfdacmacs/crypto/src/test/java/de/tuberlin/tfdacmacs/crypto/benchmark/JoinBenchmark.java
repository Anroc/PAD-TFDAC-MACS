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

public class JoinBenchmark extends UnitTestSuite {

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
    public void join_attr_1() {
        join(1);
    }

    @Test
    public void join_attr_2() {
        join(2);
    }

    @Test
    public void join_attr_4() {
        join(4);
    }

    public void join(int numAttributes) {
        SetupWrapper setupWrapper = new SetupWrapper(gpp, authorityId);
        List<AttributeValueKey> attributeValueKeys = setupWrapper.createAttributeValueKeys(numAttributes);
        int numberOfRuns = 25;

        int maxCT = 100;

        for(int numCT = 1; numCT <= maxCT; numCT += 1) {
            boolean firstRun = numCT == 1;

            BenchmarkResult rsaRun = Benchmark.rsa()
                    .numberOfRuns(numberOfRuns)
                    .numberOfUsers(1)
                    .numberOfCipherTexts(numCT)
                    .configure()
                    .benchmarkMemberJoin();

            BenchmarkResult abeRun = Benchmark.abe()
                    .numberOfRuns(numberOfRuns)
                    .numberOfUsers(1)
                    .numberOfCipherTexts(numCT)
                    .gpp(gppTestFactory.create())
                    .attributesPerUser(attributeValueKeys)
                    .policy(setupWrapper.policy())
                    .attributeValueKeyProvider(setupWrapper.attributeValueKeyProvider())
                    .authorityKeyProvider(setupWrapper.authorityKeyProvider())
                    .authorityKey(setupWrapper.authorityKey())
                    .configure()
                    .benchmarkMemberJoin();

            printResults(3, firstRun, numCT, numAttributes, rsaRun, abeRun);
        }
    }

    protected void printResults(int methodIndex, boolean firstRun, int numCt, int numerOfAttributes, BenchmarkResult rsaRun, BenchmarkResult abeRun) {
        BenchmarkCombinedResult benchmarkCombinedResult = new BenchmarkCombinedResult(rsaRun, abeRun);
        benchmarkCombinedResult.setUnit(ChronoUnit.MILLIS);
        benchmarkCombinedResult.prittyPrintStatistics(numCt);
        String fileName = "./join/" + Thread.currentThread().getStackTrace()[methodIndex].getMethodName() + ".csv";

        if(firstRun) {
            benchmarkCombinedResult.csvPrintHeaders(fileName);
        }
        benchmarkCombinedResult.csvPrintStatistics(fileName, numCt, numerOfAttributes);
    }
}
