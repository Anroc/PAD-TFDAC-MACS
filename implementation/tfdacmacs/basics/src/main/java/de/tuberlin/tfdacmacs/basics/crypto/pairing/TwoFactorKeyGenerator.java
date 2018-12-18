package de.tuberlin.tfdacmacs.basics.crypto.pairing;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.CipherText2FAUpdateKey;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.TwoFactorKey;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.TwoFactorUpdateKey;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.util.HashGenerator;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TwoFactorKeyGenerator {

    private final HashGenerator hashGenerator;

    public TwoFactorKey generate(@NonNull GlobalPublicParameter globalPublicParameter, @NonNull String uid) {
        Pairing pairing = globalPublicParameter.getPairing();

        Element alpha = pairing.getZr().newRandomElement();
        Element twoFAKey = hashGenerator.g1Hash(globalPublicParameter, uid).powZn(alpha);
        return new TwoFactorKey(alpha, twoFAKey);
    }

    public TwoFactorUpdateKey generateUpdateKey(@NonNull GlobalPublicParameter gpp,
            @NonNull TwoFactorKey.Private revokedKey, @NonNull TwoFactorKey.Private newKey, @NonNull String uid) {

        Element updateKey = hashGenerator.g1Hash(gpp, uid).powZn(
                newKey.getKey().duplicate().sub(revokedKey.getKey().duplicate())
        );

        return new TwoFactorUpdateKey(updateKey);
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
