package de.tuberlin.tfdacmacs.client.authority.client;

import de.tuberlin.tfdacmacs.client.gpp.GPPService;
import de.tuberlin.tfdacmacs.client.rest.CaClient;
import de.tuberlin.tfdacmacs.client.rest.error.InterServiceCallError;
import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AuthorityKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthorityClient {

    private final CaClient caClient;
    private final GPPService gppService;

    public Optional<AuthorityKey.Public> findAuthorityKey(@NonNull String authorityId) {
        try {
            return Optional.of(
                    new AuthorityKey.Public<>(
                        ElementConverter.convert(
                            caClient.getAuthority(authorityId).getPublicKey(),
                            gppService.getGPP().getPairing().getG1())));
        } catch(InterServiceCallError e) {
            if(e.getResponseStatus() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            } else {
                throw e;
            }
        }
    }
}
