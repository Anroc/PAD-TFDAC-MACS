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

import java.util.function.Supplier;

public class SetupBenchmark extends UnitTestSuite {

    private GlobalPublicParameter gpp;
    private static final String ATTRIBUTE_VALUE_ID = "aa.tu-berlin.de.role:student";
    private static final String USER_ID = "random@example.de";

    private static final double[][] runs = new double[6][50];

    @Before
    public void setup() {
        this.gpp = gpp();
    }

    @AfterClass
    public static void print() {
        CSVPrinter.writeCSV("setup.csv", "\"GPP Setup\","
                + "\"Authority Setup\","
                + "\"Attribute Setup\","
                + "\"User Secret Attribute\nKey Generation\","
                + "\"Two-Factor\nKey Generation\","
                + "\"Two-Factor\nSecret Key Generation\"", false);
        for (int i = 0; i < 50; i++) {
            String line =
                runs[0][i] + "," +
                runs[1][i] + "," +
                runs[2][i] + "," +
                runs[3][i] + "," +
                runs[4][i] + "," +
                runs[5][i];

            CSVPrinter.writeCSV("setup.csv", line, true);
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
        measure(0, this::gpp);
    }

    @Test
    public void benchmarkAuthoritySetup() {
        measure(1, () -> authorityKeyGenerator.generate(gpp));
    }

    @Test
    public void benchmarkAttributeSetup() {
        measure(2, () -> attributeValueKeyGenerator.generateNew(gpp, ATTRIBUTE_VALUE_ID));
    }

    @Test
    public void benchmarkSecretAttributeSetup() {
        AuthorityKey authorityKey = authorityKeyGenerator.generate(gpp);
        AttributeValueKey attributeValueKey = attributeValueKeyGenerator.generateNew(gpp, ATTRIBUTE_VALUE_ID);
        measure(3, () -> attributeValueKeyGenerator.generateUserKey(gpp, USER_ID, authorityKey.getPrivateKey(), attributeValueKey.getPrivateKey()));
    }

    @Test
    public void benchmark2FAKeySetup() {
        measure(4, () -> twoFactorKeyGenerator.generateNew(gpp));
    }

    @Test
    public void benchmark2FAUserSecretKeySetup() {
        TwoFactorKey twoFactorKey = twoFactorKeyGenerator.generateNew(gpp);
        measure(5, () -> twoFactorKeyGenerator.generatePublicKeyForUser(gpp, twoFactorKey, USER_ID));
    }

    /**
     * We are using a supplier here so that the computation does not get optimized by the JVM.
     * @param processor
     */
    private void measure(int run, Supplier<?> processor) {
        for (int i = 0; i < 50; i++) {
            runs[run][i] = (double) measureRun(processor) / 1000000.0;
            System.out.println(run + ":" + runs[run][i]);
        }
    }

    private long measureRun(Supplier<?> processor) {
        long start = System.nanoTime();
        processor.get();
        return System.nanoTime() - start;
    }

}
