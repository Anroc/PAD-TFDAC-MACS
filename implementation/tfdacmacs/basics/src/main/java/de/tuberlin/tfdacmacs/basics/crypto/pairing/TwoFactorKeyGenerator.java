package de.tuberlin.tfdacmacs.basics.crypto.pairing;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.Key;
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

    public Key generateTwoFactoryKey(@NonNull GlobalPublicParameter globalPublicParameter, @NonNull String uid) {
        Pairing pairing = globalPublicParameter.getPairing();

        Element alpha = pairing.getZr().newRandomElement();
        Element twoFAKey = hashGenerator.g1Hash(globalPublicParameter, uid).powZn(alpha);
        return new Key(alpha, twoFAKey);
    }
}
