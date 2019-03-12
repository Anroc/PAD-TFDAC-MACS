package de.tuberlin.tfdacmacs.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TwoFactorKey extends AsymmetricElementMultiKey<String> {
    public TwoFactorKey(@NonNull Element privateKey, long version) {
        super(privateKey, version);
    }

    public Set<TwoFactorKey.Public> getPublicKeyValues() {
        return super.publicKeys.entrySet()
                .stream()
                .map(entry -> new TwoFactorKey.Public(entry.getKey(), entry.getValue().getKey(), entry.getValue().getVersion()))
                .collect(Collectors.toSet());
    }

    public TwoFactorKey.Public getPublicKeyOfUser(@NonNull String userId) {
        return Optional.ofNullable(super.publicKeys.get(userId))
                .map(elementKey -> new TwoFactorKey.Public(userId, elementKey.getKey(), elementKey.getVersion()))
                .orElse(null);
    }

    public TwoFactorKey.Private getPrivateKey() {
        return new Private(getKey(), getVersion());
    }

    public static class Private extends AsymmetricElementKey.Private<TwoFactorKey> {
        public Private(@NonNull Element key, long version) {
            super(key, version);
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class Public extends AsymmetricElementKey.Public<TwoFactorKey> {

        @NotBlank
        private String userId;

        public Public(@NonNull String userId, @NonNull Element key, long version) {
            super(key, version);
            this.userId = userId;
        }

        public Public update(@NonNull TwoFactorUpdateKey twoFactorUpdateKey) {
            twoFactorUpdateKey.checkApplicablilty(this);
            Element newPublicKey = getKey().duplicate().mul(twoFactorUpdateKey.getUpdateKey());
            return update(newPublicKey);
        }

        @Override
        public Public clone() {
            return new Public(getUserId(), getKey().duplicate(), getVersion());
        }
    }
}
