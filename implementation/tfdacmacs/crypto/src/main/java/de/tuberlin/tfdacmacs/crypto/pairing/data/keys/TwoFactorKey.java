package de.tuberlin.tfdacmacs.crypto.pairing.data.keys;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TwoFactorKey extends AsymmetricElementMultiKey<String> {

    public TwoFactorKey(@NonNull Element privateKey, @NonNull Element publicKey, long version) {
        super(privateKey, version);
        this.pubKey = new SimpleElementKey(publicKey, version);
    }

    @JsonIgnore
    public Set<Secret> getSecretUserKeyValues() {
        return super.secrets.entrySet()
                .stream()
                .map(entry -> new Secret(entry.getKey(), entry.getValue().getKey(), entry.getValue().getVersion()))
                .collect(Collectors.toSet());
    }

    @JsonIgnore
    public Secret getSecretKeyOfUser(@NonNull String userId) {
        return Optional.ofNullable(super.secrets.get(userId))
                .map(elementKey -> new Secret(userId, elementKey.getKey(), elementKey.getVersion()))
                .orElse(null);
    }

    @JsonIgnore
    public TwoFactorKey.Private getPrivateKey() {
        return new Private(getKey(), getVersion());
    }

    @JsonIgnore
    public TwoFactorKey.Public getPublicKey() {
        return new Public(getPubKey().getKey(), getPubKey().getVersion());
    }

    @EqualsAndHashCode(callSuper = true)
    public static class Private extends AsymmetricElementKey.Private<TwoFactorKey> {
        public Private(@NonNull Element key, long version) {
            super(key, version);
        }
    }

    @EqualsAndHashCode(callSuper = true)
    public static class Public extends AsymmetricElementKey.Public<TwoFactorKey> {
        public Public(@NonNull Element key, long version) {
            super(key, version);
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class Secret extends AsymmetricElementKey.Public<TwoFactorKey> {

        @NotBlank
        private String userId;

        public Secret(@NonNull String userId, @NonNull Element key, long version) {
            super(key, version);
            this.userId = userId;
        }

        public Secret update(@NonNull TwoFactorUpdateKey twoFactorUpdateKey) {
            twoFactorUpdateKey.checkApplicablilty(this);
            Element newPublicKey = getKey().duplicate().mul(twoFactorUpdateKey.getUpdateKey());
            return update(newPublicKey);
        }

        @Override
        public Secret clone() {
            return new Secret(getUserId(), getKey().duplicate(), getVersion());
        }
    }
}
