package de.tuberlin.tfdacmacs.crypto.pairing;

import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.VersionedID;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.*;
import de.tuberlin.tfdacmacs.crypto.pairing.util.HashGenerator;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class TwoFactorKeyGenerator {

    private final HashGenerator hashGenerator;

    public TwoFactorKey generateNew(@NonNull GlobalPublicParameter globalPublicParameter, @NonNull String... uids) {
        return generate(globalPublicParameter, 0L, uids);
    }

    public TwoFactorKey generateNext(@NonNull GlobalPublicParameter globalPublicParameter, TwoFactorKey currentTwoFactorKey, String... uids) {
        return generate(globalPublicParameter, currentTwoFactorKey.getVersion() + 1, uids);
    }

    private TwoFactorKey generate(GlobalPublicParameter globalPublicParameter, long version, String... uids) {
        Pairing pairing = globalPublicParameter.getPairing();

        Element alpha = pairing.getZr().newRandomElement();
        Element g_alpha = globalPublicParameter.getG().getImmutable().powZn(alpha.duplicate());
        TwoFactorKey twoFactorKey = new TwoFactorKey(alpha, g_alpha, version);

        Arrays.stream(uids).forEach(uid -> generateSecretKeyForUser(globalPublicParameter, twoFactorKey, uid));

        return twoFactorKey;
    }

    public TwoFactorKey generateSecretKeyForUser(@NonNull GlobalPublicParameter globalPublicParameter,
            @NonNull TwoFactorKey twoFactorKey, @NonNull String uid) {
        Element twoFAKey = hashGenerator.g1Hash(globalPublicParameter, uid).powZn(twoFactorKey.getPrivateKey().getKey());
        twoFactorKey.putPublicKey(uid, new SimpleElementKey(twoFAKey, twoFactorKey.getVersion()));
        return twoFactorKey;
    }

    public TwoFactorUpdateKey generateUpdateKey(@NonNull GlobalPublicParameter gpp,
            @NonNull TwoFactorKey.Private revokedKey, @NonNull TwoFactorKey.Private newKey, @NonNull String uid) {

        Element updateKey = hashGenerator.g1Hash(gpp, uid).powZn(
                newKey.getKey().duplicate().sub(revokedKey.getKey().duplicate())
        );

        return new TwoFactorUpdateKey(uid, updateKey, revokedKey.getVersion());
    }

    public CipherText2FAUpdateKey generateCipherTextUpdateKey(@NonNull TwoFactorKey.Private revokedKey,
            @NonNull TwoFactorKey.Private newKey, @NonNull AttributeValueKey.Public attributeValuePublicKey,
            @NonNull String oid) {

        Element updateKey = attributeValuePublicKey.getKey().duplicate().powZn(
                newKey.getKey().duplicate().sub(revokedKey.getKey().duplicate())
        );
        return new CipherText2FAUpdateKey(
                updateKey,
                new VersionedID(attributeValuePublicKey.getAttributeValueId(), attributeValuePublicKey.getVersion()),
                oid,
                revokedKey.getVersion()
        );
    }
}
