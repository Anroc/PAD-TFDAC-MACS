package de.tuberlin.tfdacmacs.crypto.benchmark;

import de.tuberlin.tfdacmacs.crypto.UnitTestSuite;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import lombok.Getter;
import lombok.Setter;
import org.junit.Before;
import org.junit.BeforeClass;

import java.time.temporal.ChronoUnit;
import java.util.Locale;

public abstract class EncryptionDecryptionBenchmark extends UnitTestSuite {

    protected GlobalPublicParameter gpp;

    @Getter
    @Setter
    protected int numUsers;
    protected final String authorityId = "aa.tu-berlin.de";
    private final static int DEFAULT_NUM_USERS = 250;

    public EncryptionDecryptionBenchmark(int numUsers) {
        this.numUsers = numUsers;
    }

    public EncryptionDecryptionBenchmark() {
        this(DEFAULT_NUM_USERS);
    }

    @BeforeClass
    public static void init() {
        Locale.setDefault(Locale.US);
    }

    @Before
    public void setup() {
        this.gpp = gppTestFactory.create();
    }

    abstract String getFileDir();

    protected void incrementAttributes(int attributesPerUser, boolean andPolicy) {
        SetupWrapper setupWrapper = new SetupWrapper(gpp, authorityId);
        setupWrapper.setPolicy(andPolicy);
        int numberOfRuns = 25;
        int stepSize  = 10;

        int buffer = 1;
        for(int userCount = 1; userCount <= this.numUsers; userCount += stepSize ) {
            boolean firstRun = userCount == 1;

            if(buffer / attributesPerUser > 0) {
                setupWrapper.createAttributeValueKeys(buffer / attributesPerUser);
                buffer = buffer % attributesPerUser;
            } else if(userCount < attributesPerUser && firstRun) {
                setupWrapper.createAttributeValueKeys(1);
            }

            BenchmarkResult rsaRun = Benchmark.rsa()
                    .numberOfRuns(numberOfRuns)
                    .numberOfUsers(userCount)
                    .configure()
                    .preHeat(firstRun)
                    .benchmarkEncrypt();

            BenchmarkResult abeRun = Benchmark.abe()
                    .numberOfRuns(numberOfRuns)
                    .numberOfUsers(userCount)
                    .gpp(gppTestFactory.create())
                    .attributesPerUser(setupWrapper.createdKeys())
                    .policy(setupWrapper.policy())
                    .attributeValueKeyProvider(setupWrapper.attributeValueKeyProvider())
                    .authorityKeyProvider(setupWrapper.authorityKeyProvider())
                    .authorityKey(setupWrapper.authorityKey())
                    .configure()
                    .preHeat(firstRun)
                    .benchmarkEncrypt();

            printResults(3, firstRun, userCount, setupWrapper.createdKeys().size(), rsaRun, abeRun);

            buffer += stepSize;
        }
    }

    protected void printResults(int methodIndex, boolean firstRun, int numUsers, int numerOfAttributes, BenchmarkResult rsaRun, BenchmarkResult abeRun) {
        BenchmarkCombinedResult benchmarkCombinedResult = new BenchmarkCombinedResult(rsaRun, abeRun);
        benchmarkCombinedResult.setUnit(ChronoUnit.MILLIS);
        benchmarkCombinedResult.prittyPrintStatistics(numUsers);
        String fileName = "./" + getFileDir() + "/" + Thread.currentThread().getStackTrace()[methodIndex].getMethodName() + ".csv";

        if(firstRun) {
            benchmarkCombinedResult.csvPrintHeaders(fileName);
        }
        benchmarkCombinedResult.csvPrintStatistics(fileName, numUsers, numerOfAttributes);
    }
}
