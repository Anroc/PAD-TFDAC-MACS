package de.tuberlin.tfdacmacs.crypto.benchmark;

import de.tuberlin.tfdacmacs.crypto.benchmark.factory.ABEUserFactory;
import de.tuberlin.tfdacmacs.crypto.benchmark.factory.RSAUserFactory;
import de.tuberlin.tfdacmacs.crypto.benchmark.pairing.ABEGroup;
import de.tuberlin.tfdacmacs.crypto.benchmark.pairing.ABEUser;
import de.tuberlin.tfdacmacs.crypto.benchmark.rsa.RSAGroup;
import de.tuberlin.tfdacmacs.crypto.benchmark.rsa.RSAUser;
import de.tuberlin.tfdacmacs.crypto.benchmark.utils.SetUtils;
import de.tuberlin.tfdacmacs.crypto.pairing.data.DNFAccessPolicy;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AuthorityKey;
import de.tuberlin.tfdacmacs.crypto.pairing.policy.AccessPolicyParser;
import de.tuberlin.tfdacmacs.crypto.pairing.policy.AttributeValueKeyProvider;
import de.tuberlin.tfdacmacs.crypto.pairing.policy.AuthorityKeyProvider;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class Benchmark {

    public static RSAConfiguredBenchmark.RSAConfiguredBenchmarkBuilder rsa() {
        return RSAConfiguredBenchmark.builder();
    }

    public static ABEConfiguredBenchmark.ABEConfiguredBenchmarkBuilder abe() {
        return ABEConfiguredBenchmark.builder();
    }

    @Data
    private static abstract class ConfiguredBenchmark {
        private final int numberOfRuns;
        private final int numberOfUsers;

        private final byte[] content = "Sample Text that need to be encrypted.".getBytes();

        public abstract BenchmarkResult run();

        protected BenchmarkResult doRun(Supplier<Long> method) {
            BenchmarkResult result = new BenchmarkResult();

            for (int i = 0; i < getNumberOfRuns(); i++) {
                result.addRun(method.get());
            }
            return result;
        }
    }

    public static class RSAConfiguredBenchmark extends ConfiguredBenchmark {

        private final RSAUserFactory rsaUserFactory = new RSAUserFactory();
        private RSAGroup rsaGroup = null;
        private RSAUser member = null;

        @Builder(buildMethodName = "configure")
        public RSAConfiguredBenchmark(int numberOfRuns, int numberOfUsers) {
            super(numberOfRuns, numberOfUsers);
        }

        @Override
        public BenchmarkResult run() {
            if(rsaGroup == null) {
                Set<RSAUser> rsaUsers = rsaUserFactory.create(getNumberOfUsers());

                this.rsaGroup = new RSAGroup();
                this.rsaGroup.setMembers(rsaUsers);
                this.member = SetUtils.first(rsaUsers);
            }

            return doRun(() -> rsaGroup.encrypt(getContent(), member));
        }
    }

    public static class ABEConfiguredBenchmark extends ConfiguredBenchmark {

        private final ABEUserFactory abeUserFactory;
        private final AccessPolicyParser accessPolicyParser;
        private final DNFAccessPolicy dnfAccessPolicy;
        private final GlobalPublicParameter gpp;

        private ABEGroup abeGroup = null;
        private ABEUser member = null;

        @Builder(buildMethodName = "configure")
        public ABEConfiguredBenchmark(
                int numberOfRuns,
                int numberOfUsers,
                String policy,
                GlobalPublicParameter gpp,
                AttributeValueKeyProvider attributeValueKeyProvider,
                AuthorityKeyProvider authorityKeyProvider,
                AuthorityKey.Private authorityPrivateKey,
                List<AttributeValueKey> attributesPerUser) {
            super(numberOfRuns, numberOfUsers);
            this.accessPolicyParser = new AccessPolicyParser(attributeValueKeyProvider, authorityKeyProvider);
            this.dnfAccessPolicy = accessPolicyParser.parse(policy);

            this.abeUserFactory = new ABEUserFactory(gpp, authorityPrivateKey, attributesPerUser);
            this.gpp = gpp;

        }

        @Override
        public BenchmarkResult run() {
            if(abeGroup == null) {

                Set<ABEUser> rsaUsers = abeUserFactory.create(getNumberOfUsers());

                abeGroup = new ABEGroup(gpp, dnfAccessPolicy);
                abeGroup.setMembers(rsaUsers);
                member = SetUtils.first(rsaUsers);
            }

            return doRun(() -> abeGroup.encrypt(getContent(), member));
        }
    }
}
