package de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.NonNull;

import java.util.Set;
import java.util.stream.Collectors;

public class TwoFactorKey extends AsymmetricElementMultiKey<String> {
    public TwoFactorKey(@NonNull Element privateKey) {
        super(privateKey);
    }

    public Set<TwoFactorKey.Public> getPublicKeys() {
        return super.publicKeys.entrySet()
                .stream()
                .map(entry -> new TwoFactorKey.Public<>(entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());
    }

    public TwoFactorKey.Public getPublicKeyOfUser(@NonNull String userId) {
        return new TwoFactorKey.Public<>(userId, super.publicKeys.get(userId));
    }

    public TwoFactorKey.Private getPrivateKey() {
        return new Private(getSecretKey());
    }

    public static class Private<T> extends AsymmetricElementKey.Private<TwoFactorKey> {
        public Private(@NonNull Element key) {
            super(key);
        }
    }

    @Data
    public static class Public<T> extends AsymmetricElementKey.Public<TwoFactorKey> {

        private final String userId;

        public Public(@NonNull String userId, @NonNull Element key) {
            super(key);
            this.userId = userId;
        }

        public Public update(@NonNull TwoFactorUpdateKey twoFactorUpdateKey) {
            Element newPublicKey = getKey().duplicate().mul(twoFactorUpdateKey.getUpdateKey());
            return new Public(this.getUserId(), newPublicKey);
        }
    }
}
