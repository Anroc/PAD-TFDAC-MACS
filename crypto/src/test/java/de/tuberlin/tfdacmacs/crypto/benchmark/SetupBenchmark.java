package de.tuberlin.tfdacmacs.crypto.benchmark;

import de.tuberlin.tfdacmacs.crypto.UnitTestSuite;
import de.tuberlin.tfdacmacs.crypto.benchmark.utils.CSVPrinter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AuthorityKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorKey;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import static de.tuberlin.tfdacmacs.crypto.benchmark.MeasureSuite.NUM_RUNS;
import static de.tuberlin.tfdacmacs.crypto.benchmark.MeasureSuite.measure;

public class SetupBenchmark extends UnitTestSuite {

    private GlobalPublicParameter gpp;
    private static final String ATTRIBUTE_VALUE_ID = "aa.tu-berlin.de.role:student";
    private static final String USER_ID = "random@example.de";

    private static final double[][] runs = new double[6][NUM_RUNS];
    private static final String FILE_NAME = "./setup/setup.csv";

    @Before
    public void setup() {
        this.gpp = gpp();
    }

    @AfterClass
    public static void print() {
        CSVPrinter.writeCSV(FILE_NAME, "\"GPP Setup\","
                + "\"Authority Setup\","
                + "\"Attribute Setup\","
                + "\"User Secret Attribute\nKey Generation\","
                + "\"Two-Factor\nKey Generation\","
                + "\"Two-Factor\nSecret Key Generation\"", false);
        for (int i = 0; i < NUM_RUNS; i++) {
            String line =
                runs[0][i] + "," +
                runs[1][i] + "," +
                runs[2][i] + "," +
                runs[3][i] + "," +
                runs[4][i] + "," +
                runs[5][i];

            CSVPrinter.writeCSV(FILE_NAME, line, true);
        }
    }

    private GlobalPublicParameter gpp() {
        PairingParameters pairingParameters = pairingGenerator.generateNewTypeACurveParameter();
        Pairing pairing = pairingGenerator.setupPairing(pairingParameters);
        return new GlobalPublicParameter(
                pairing, pairingParameters, pairing.getG1().newRandomElement().getImmutable());
    }

    @Test
    public void benchmarkGPPInit() {
        measure(runs,0, this::gpp);
    }

    @Test
    public void benchmarkAuthoritySetup() {
        measure(runs,1, () -> authorityKeyGenerator.generate(gpp));
    }

    @Test
    public void benchmarkAttributeSetup() {
        measure(runs, 2, () -> attributeValueKeyGenerator.generateNew(gpp, ATTRIBUTE_VALUE_ID));
    }

    @Test
    public void benchmarkSecretAttributeSetup() {
        AuthorityKey authorityKey = authorityKeyGenerator.generate(gpp);
        AttributeValueKey attributeValueKey = attributeValueKeyGenerator.generateNew(gpp, ATTRIBUTE_VALUE_ID);
        measure(runs, 3, () -> attributeValueKeyGenerator.generateUserKey(gpp, USER_ID, authorityKey.getPrivateKey(), attributeValueKey.getPrivateKey()));
    }

    @Test
    public void benchmark2FAKeySetup() {
        measure(runs,4, () -> twoFactorKeyGenerator.generateNew(gpp));
    }

    @Test
    public void benchmark2FAUserSecretKeySetup() {
        TwoFactorKey twoFactorKey = twoFactorKeyGenerator.generateNew(gpp);
        measure(runs,5, () -> twoFactorKeyGenerator.generateSecretKeyForUser(gpp, twoFactorKey, USER_ID));
    }

}
