package de.tuberlin.tfdacmacs.client.twofactor;

import de.tuberlin.tfdacmacs.client.rest.session.Session;
import de.tuberlin.tfdacmacs.client.twofactor.client.TwoFactorAuthenticationClient;
import de.tuberlin.tfdacmacs.client.twofactor.data.PublicTwoFactorAuthentication;
import de.tuberlin.tfdacmacs.client.twofactor.data.TwoFactorAuthentication;
import de.tuberlin.tfdacmacs.client.twofactor.db.PublicTwoFactorAuthenticationDB;
import de.tuberlin.tfdacmacs.client.twofactor.db.TwoFactorAuthenticationDB;
import de.tuberlin.tfdacmacs.crypto.pairing.data.DataOwner;
import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TwoFactorAuthenticationService {

    private final TwoFactorAuthenticationDB twoFactorAuthenticationDB;
    private final PublicTwoFactorAuthenticationDB publicTwoFactorAuthenticationDB;
    private final Session session;
    private final TwoFactorAuthenticationClient client;

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

    public TwoFactorAuthentication trust(String... userIds) {
        Optional<TwoFactorAuthentication> twoFactorAuthenticationOptional
                = findTwoFactorAuthentication();

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

    public TwoFactorAuthentication distrust(String... userIds) {
        TwoFactorAuthentication twoFactorAuthentication = findTwoFactorAuthentication()
                .orElseThrow(() -> new IllegalStateException(
                        "Can not distrust users that I don't trust in the first place."
                ));

        List<String> retrainedUserIds = Arrays.asList(userIds);

        Map<String, Element> publicKeys = twoFactorAuthentication.getTwoFactorKey().getPublicKeys();
        Set<String> revokedUserIds = publicKeys.keySet();
        revokedUserIds.removeAll(retrainedUserIds);
        retrainedUserIds.forEach(publicKeys::remove);

        twoFactorAuthentication = twoFactorKeyManager.update(twoFactorAuthentication);

        twoFactorAuthenticationDB.update(twoFactorAuthentication.getOwnerId(), twoFactorAuthentication);
        return twoFactorAuthentication;
    }

    public void update() {
        client.updateTwoFactorKeys().forEach(publicTwoFactorAuthentication ->
                publicTwoFactorAuthenticationDB.upsert(
                        publicTwoFactorAuthentication.getOwnerId(),
                        publicTwoFactorAuthentication)
        );
    }

    public List<PublicTwoFactorAuthentication> getAllPublicTwoFactorAuthentications() {
        return publicTwoFactorAuthenticationDB.findAll().collect(Collectors.toList());
    }

    public Optional<PublicTwoFactorAuthentication> findPublicTwoFactorAuthentication(@NonNull String ownerId) {
        return publicTwoFactorAuthenticationDB.find(ownerId);
    }

    public Optional<TwoFactorAuthentication> findTwoFactorAuthentication() {
        return twoFactorAuthenticationDB.find(session.getEmail());
    }
}
