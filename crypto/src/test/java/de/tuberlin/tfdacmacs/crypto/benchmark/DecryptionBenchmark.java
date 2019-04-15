package de.tuberlin.tfdacmacs.crypto.benchmark;

import de.tuberlin.tfdacmacs.crypto.UnitTestSuite;
import de.tuberlin.tfdacmacs.crypto.benchmark.factory.ABEUserFactory;
import de.tuberlin.tfdacmacs.crypto.benchmark.factory.RSAUserFactory;
import de.tuberlin.tfdacmacs.crypto.benchmark.pairing.ABEUser;
import de.tuberlin.tfdacmacs.crypto.benchmark.rsa.RSAUser;
import de.tuberlin.tfdacmacs.crypto.benchmark.utils.CSVPrinter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.*;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorKey;
import de.tuberlin.tfdacmacs.crypto.pairing.policy.AccessPolicyParser;
import de.tuberlin.tfdacmacs.crypto.rsa.AsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.StringSymmetricCryptEngine;
import de.tuberlin.tfdacmacs.crypto.rsa.SymmetricCryptEngine;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.util.UUID;

import static de.tuberlin.tfdacmacs.crypto.benchmark.MeasureSuite.NUM_RUNS;
import static de.tuberlin.tfdacmacs.crypto.benchmark.MeasureSuite.measure;

public class DecryptionBenchmark extends UnitTestSuite {

    private SetupWrapper setupWrapper;
    private AccessPolicyParser accessPolicyParser;
    private GlobalPublicParameter gpp;
    private ABEUser abeUser;
    private final static String content = "Hello World!";

    private static double[][] runs = new double[3][NUM_RUNS];
    private static final String FILE_NAME = "./decrypt/decrypt.csv";

    @Before
    public void setup() {
        setupWrapper = new SetupWrapper(gppTestFactory.create(), "aa.tu-berlin.de");
        accessPolicyParser = new AccessPolicyParser(
                setupWrapper.attributeValueKeyProvider(),
                setupWrapper.authorityKeyProvider()
        );

        gpp = gppTestFactory.getGlobalPublicParameter();
        ABEUserFactory abeUserFactory = new ABEUserFactory(
                gpp,
                setupWrapper.authorityKey().getPrivateKey(),
                setupWrapper.createAttributeValueKeys(1)
        );

        abeUser = abeUserFactory.create(1).get(0);
    }

    @AfterClass
    public static void print() {
        CSVPrinter.writeCSV(FILE_NAME, "\"PAD-TFDAC-MACS\nDecryption\",\"PAD-TFDAC-MACS\nDecryption with 2FA\",\"RSA decryption\"", false);
        for (int i = 0; i < NUM_RUNS; i++) {
            String line = runs[0][i] + "," +
                            runs[1][i] + "," +
                            runs[2][i];

            CSVPrinter.writeCSV(FILE_NAME, line, true);
        }
    }

    @Test
    public void benchmarkDecryption() {
        DNFAccessPolicy dnfAccessPolicy = accessPolicyParser.parse(setupWrapper.policy());
        DNFCipherText dnfCipherText = pairingCryptEngine
                .encrypt(content.getBytes(), dnfAccessPolicy, gpp, null);

        byte[] encryptedContent = dnfCipherText.getFile().getData();
        CipherText cipherText = dnfCipherText.getCipherTexts().get(0);

        measure(runs, 0,
                () -> pairingCryptEngine.decrypt(
                        encryptedContent,
                        cipherText,
                        gpp,
                        abeUser.getId(),
                        abeUser.getAttributes(),
                        null)
        );
    }

    @Test
    public void benchmarkDecryption2FA() {
        DNFAccessPolicy dnfAccessPolicy = accessPolicyParser.parse(setupWrapper.policy());
        String ownerId = UUID.randomUUID().toString();
        TwoFactorKey twoFactorKey = twoFactorKeyGenerator.generateNew(gpp);
        TwoFactorKey.Secret twoFactorKeySecret = twoFactorKeyGenerator.generateSecretKeyForUser(gpp, twoFactorKey, abeUser.getId()).getSecretKeyOfUser(abeUser.getId());
        DNFCipherText dnfCipherText = pairingCryptEngine
                .encrypt(content.getBytes(), dnfAccessPolicy, gpp, new DataOwner(ownerId, twoFactorKey.getPrivateKey()));

        byte[] encryptedContent = dnfCipherText.getFile().getData();
        CipherText cipherText = dnfCipherText.getCipherTexts().get(0);


        measure(runs, 1,
                () -> pairingCryptEngine.decrypt(
                        encryptedContent,
                        cipherText,
                        gpp,
                        abeUser.getId(),
                        abeUser.getAttributes(),
                        twoFactorKeySecret)
        );
    }

    @Test
    public void benchmarkDecrytionRSA() throws BadPaddingException, InvalidKeyException, IllegalBlockSizeException {
        RSAUserFactory rsaUserFactory = new RSAUserFactory();
        RSAUser rsaUser = rsaUserFactory.create(1).get(0);
        AsymmetricCryptEngine asymmetricCryptEngine = new StringAsymmetricCryptEngine();
        SymmetricCryptEngine symmetricCryptEngine = new StringSymmetricCryptEngine();;

        String fileKey = asymmetricCryptEngine.encryptRaw(symmetricCryptEngine.getSymmetricCipherKey().getEncoded(), rsaUser.getPublicKey());
        byte[] bytes = symmetricCryptEngine.encryptRaw(content.getBytes(), symmetricCryptEngine.getSymmetricCipherKey());

        measure(runs, 2, () -> {
            try {
                Key key = symmetricCryptEngine.createKeyFromBytes(asymmetricCryptEngine.decryptRaw(fileKey, rsaUser.getPrivateKey()));
                return symmetricCryptEngine.decryptRaw(bytes, key);
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
            return null;
        });

    }
}
