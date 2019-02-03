package de.tuberlin.tfdacmacs.client.twofactor;

import de.tuberlin.tfdacmacs.client.rest.session.Session;
import de.tuberlin.tfdacmacs.client.twofactor.data.TwoFactorAuthentication;
import de.tuberlin.tfdacmacs.client.twofactor.db.TwoFactorAuthenticationDB;
import de.tuberlin.tfdacmacs.crypto.pairing.data.DataOwner;
import it.unisa.dia.gas.jpbc.Element;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TwoFactorAuthenticationService {

    private final TwoFactorAuthenticationDB twoFactorAuthenticationDB;
    private final Session session;

    private final TwoFactorKeyManager twoFactorKeyManager;

    public DataOwner getDataOwner() {
        TwoFactorAuthentication twoFactorAuthentication = twoFactorAuthenticationDB.find(session.getEmail())
                .orElseGet(this::createNewTwoFactorAuthentication);

        return twoFactorAuthentication.toDataOwner();
    }

    private TwoFactorAuthentication createNewTwoFactorAuthentication(String... userIds) {
        TwoFactorAuthentication twoFactorAuthentication = new TwoFactorAuthentication(
                session.getEmail(),
                twoFactorKeyManager.generate(userIds)
        );

        twoFactorAuthenticationDB.insert(twoFactorAuthentication.getOwnerId(), twoFactorAuthentication);
        return twoFactorAuthentication;
    }

    public TwoFactorAuthentication upsertTwoFactorAuthentication(String... userIds) {
        Optional<TwoFactorAuthentication> twoFactorAuthenticationOptional
                = twoFactorAuthenticationDB.find(session.getEmail());

        if(! twoFactorAuthenticationOptional.isPresent()) {
            return createNewTwoFactorAuthentication(userIds);
        }

        TwoFactorAuthentication twoFactorAuthentication = twoFactorAuthenticationOptional.get();

        Map<String, Element> publicKeys = twoFactorAuthentication.getTwoFactorKey().getPublicKeys();
        Arrays.stream(userIds)
                .filter(userId -> ! publicKeys.containsKey(userId))
                .forEach(userId -> twoFactorKeyManager.generatePublicKeyForUser(
                        twoFactorAuthentication.getTwoFactorKey(),
                        userId));

        twoFactorAuthenticationDB.update(twoFactorAuthentication.getOwnerId(), twoFactorAuthentication);
        return twoFactorAuthentication;
    }

}
