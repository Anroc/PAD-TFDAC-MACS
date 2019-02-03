package de.tuberlin.tfdacmacs.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.*;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TwoFactorKey extends AsymmetricElementMultiKey<String> {
    public TwoFactorKey(@NonNull Element privateKey) {
        super(privateKey);
    }

    public Set<TwoFactorKey.Public> getPublicKeyValues() {
        return super.publicKeys.entrySet()
                .stream()
                .map(entry -> new TwoFactorKey.Public(entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());
    }

    public TwoFactorKey.Public getPublicKeyOfUser(@NonNull String userId) {
        return new TwoFactorKey.Public(userId, super.publicKeys.get(userId));
    }

    public TwoFactorKey.Private getPrivateKey() {
        return new Private(getKey());
    }

    public static class Private extends AsymmetricElementKey.Private<TwoFactorKey> {
        public Private(@NonNull Element key) {
            super(key);
        }
    }

    @Data
    public static class Public extends AsymmetricElementKey.Public<TwoFactorKey> {

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
