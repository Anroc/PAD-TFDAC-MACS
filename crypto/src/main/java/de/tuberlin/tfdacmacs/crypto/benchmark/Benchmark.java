package de.tuberlin.tfdacmacs.crypto.benchmark;

import de.tuberlin.tfdacmacs.crypto.benchmark.factory.ABEUserFactory;
import de.tuberlin.tfdacmacs.crypto.benchmark.factory.RSAUserFactory;
import de.tuberlin.tfdacmacs.crypto.benchmark.pairing.ABEGroup;
import de.tuberlin.tfdacmacs.crypto.benchmark.pairing.ABEUser;
import de.tuberlin.tfdacmacs.crypto.benchmark.rsa.RSAGroup;
import de.tuberlin.tfdacmacs.crypto.benchmark.rsa.RSAUser;
import de.tuberlin.tfdacmacs.crypto.pairing.data.DNFAccessPolicy;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AuthorityKey;
import de.tuberlin.tfdacmacs.crypto.pairing.policy.AccessPolicyParser;
import de.tuberlin.tfdacmacs.crypto.pairing.policy.AttributeValueKeyProvider;
import de.tuberlin.tfdacmacs.crypto.pairing.policy.AuthorityKeyProvider;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class Benchmark {

    public static RSAConfiguredBenchmark.RSAConfiguredBenchmarkBuilder rsa() {
        return RSAConfiguredBenchmark.builder();
    }

    public static ABEConfiguredBenchmark.ABEConfiguredBenchmarkBuilder abe() {
        return ABEConfiguredBenchmark.builder();
    }

    @Data
    public static abstract class ConfiguredBenchmark {
        private final int numberOfRuns;
        private final int numberOfUsers;
        private final int numberOfCipherTexts;

        private final byte[] content = "Sample text that needs to be encrypted.".getBytes();

        public abstract BenchmarkResult benchmarkEncrypt();
        public abstract BenchmarkResult benchmarkMemberJoin();
        public abstract BenchmarkResult benchmarkMemberLeave();
        public abstract Group getGroup();
        public abstract Group doSetupGroup();
        protected abstract void doPreHeat();

        public ConfiguredBenchmark preHeat(boolean shouldPreheat) {
            if(shouldPreheat) {
                initGroupIfNessecary();
                for (int i = 0; i < 2000; i++) {
                    doPreHeat();
                }
            }
            return this;
        }

        protected Group setupGroup() {
            Group group = doSetupGroup();
            IntStream.range(0, getNumberOfCipherTexts()).forEach(
                    (i) -> group.encrypt(("content_" + i).getBytes(), (User) group.getMembers().get(0))
            );

            return group;
        }


        protected BenchmarkResult doRun(Supplier<RunTimeResult> method) {
           return doRun(true, method);
        }

        protected BenchmarkResult doRun(boolean setupGroup, Supplier<RunTimeResult> method) {
            BenchmarkResult result = new BenchmarkResult();

            initGroupIfNessecary();
            for (int i = 0; i < getNumberOfRuns(); i++) {
                if(setupGroup) {
                    setupGroup();
                }
                result.addRun(method.get());
            }
            return result;
        }

        private void initGroupIfNessecary() {
            if(getGroup() == null) {
                setupGroup();
            }
        }
    }

    public static class RSAConfiguredBenchmark extends ConfiguredBenchmark {

        private final RSAUserFactory rsaUserFactory = new RSAUserFactory();

        @Getter(onMethod = @__(@Override))
        private RSAGroup group = null;
        private RSAUser member = null;

        @Builder(buildMethodName = "configure")
        public RSAConfiguredBenchmark(int numberOfRuns, int numberOfUsers, int numberOfCipherTexts) {
            super(numberOfRuns, numberOfUsers, numberOfCipherTexts);
        }

        @Override
        public BenchmarkResult benchmarkEncrypt() {
            return doRun(false, () -> group.encrypt(getContent(), member));
        }

        @Override
        public BenchmarkResult benchmarkMemberJoin() {
            return doRun(() -> group.join(rsaUserFactory.create(1).get(0)));
        }

        @Override
        public BenchmarkResult benchmarkMemberLeave() {
            return doRun(() -> group.leave(group.getMembers().get(0)));
        }

        @Override
        public void doPreHeat() {
            group.encrypt(getContent(), member);
        }

        @Override
        public Group doSetupGroup() {
            List<RSAUser> rsaUsers = rsaUserFactory.create(getNumberOfUsers());

            this.group = new RSAGroup();
            this.group.setMembers(rsaUsers);
            this.member = rsaUsers.get(0);
            return this.group;
        }
    }

    public static class ABEConfiguredBenchmark extends ConfiguredBenchmark {

        private final ABEUserFactory abeUserFactory;
        private final AccessPolicyParser accessPolicyParser;
        private DNFAccessPolicy dnfAccessPolicy;
        private final GlobalPublicParameter gpp;

        private final AuthorityKey authorityKey;
        private final List<AttributeValueKey> attributesPerUser;

        private final boolean use2FA;

        @Getter(onMethod = @__(@Override))
        private ABEGroup group = null;
        private ABEUser member = null;

        @Builder(buildMethodName = "configure")
        public ABEConfiguredBenchmark(
                int numberOfRuns,
                int numberOfUsers,
                int numberOfCipherTexts,
                String policy,
                GlobalPublicParameter gpp,
                AttributeValueKeyProvider attributeValueKeyProvider,
                AuthorityKeyProvider authorityKeyProvider,
                AuthorityKey authorityKey,
                List<AttributeValueKey> attributesPerUser,
                boolean use2FA) {
            super(numberOfRuns, numberOfUsers, numberOfCipherTexts);
            this.accessPolicyParser = new AccessPolicyParser(attributeValueKeyProvider, authorityKeyProvider);
            this.dnfAccessPolicy = accessPolicyParser.parse(policy);

            this.abeUserFactory = new ABEUserFactory(gpp, authorityKey.getPrivateKey(), attributesPerUser);
            this.gpp = gpp;

            this.authorityKey = authorityKey;
            this.attributesPerUser = new ArrayList<>(attributesPerUser);
            this.use2FA = use2FA;
        }

        @Override
        public BenchmarkResult benchmarkEncrypt() {
            return doRun(false, () -> group.encrypt(getContent(), member));
        }

        @Override
        public BenchmarkResult benchmarkMemberJoin() {
            return doRun(() -> group.join(new ABEUser(UUID.randomUUID().toString(), new HashSet<>())));
        }

        @Override
        public BenchmarkResult benchmarkMemberLeave() {
            // first user must stay in group as the data owner
            return doRun(() -> group.leave(group.getMembers().get(1)));
        }

        @Override
        protected void doPreHeat() {
            group.encrypt(getContent(), member);
        }

        public Group doSetupGroup() {
            List<ABEUser> abeUsers = abeUserFactory.create(getNumberOfUsers());

            this.group = new ABEGroup(gpp, dnfAccessPolicy, attributesPerUser, authorityKey);
            this.group.setMembers(abeUsers);
            member = abeUsers.get(0);
            member.setUseTowFactorKey(this.use2FA);
            return this.group;
        }
    }
}
