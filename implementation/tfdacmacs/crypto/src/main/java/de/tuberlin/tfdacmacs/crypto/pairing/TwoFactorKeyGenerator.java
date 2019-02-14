package de.tuberlin.tfdacmacs.crypto.pairing;

import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.CipherText2FAUpdateKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.TwoFactorUpdateKey;
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

    public TwoFactorKey generate(@NonNull GlobalPublicParameter globalPublicParameter, @NonNull String... uids) {
        Pairing pairing = globalPublicParameter.getPairing();

        Element alpha = pairing.getZr().newRandomElement();
        TwoFactorKey twoFactorKey = new TwoFactorKey(alpha);

        Arrays.stream(uids).forEach(uid -> generatePublicKeyForUser(globalPublicParameter, twoFactorKey, uid));

        return twoFactorKey;
    }

    public TwoFactorKey generatePublicKeyForUser(@NonNull GlobalPublicParameter globalPublicParameter,
            @NonNull TwoFactorKey twoFactorKey, @NonNull String uid) {
        Element twoFAKey = hashGenerator.g1Hash(globalPublicParameter, uid).powZn(twoFactorKey.getPrivateKey().getKey());
        twoFactorKey.putPublicKey(uid, twoFAKey);
        return twoFactorKey;
    }

    public TwoFactorUpdateKey generateUpdateKey(@NonNull GlobalPublicParameter gpp,
            @NonNull TwoFactorKey.Private revokedKey, @NonNull TwoFactorKey.Private newKey, @NonNull String uid) {

        Element updateKey = hashGenerator.g1Hash(gpp, uid).powZn(
                newKey.getKey().duplicate().sub(revokedKey.getKey().duplicate())
        );

        return new TwoFactorUpdateKey(uid, updateKey);
    }

    public CipherText2FAUpdateKey generateCipherTextUpdateKey(@NonNull TwoFactorKey.Private revokedKey,
            @NonNull TwoFactorKey.Private newKey, @NonNull AttributeValueKey.Public attributeValuePublicKey,
            @NonNull String oid) {

        Element updateKey = attributeValuePublicKey.getKey().duplicate().powZn(
                newKey.getKey().duplicate().sub(revokedKey.getKey().duplicate())
        );
        return new CipherText2FAUpdateKey(updateKey,  attributeValuePublicKey.getAttributeValueId(), oid);
    }
}
