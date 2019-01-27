package de.tuberlin.tfdacmacs.attributeauthority.authority.client;

import de.tuberlin.tfdacmacs.attributeauthority.client.CAClient;
import de.tuberlin.tfdacmacs.attributeauthority.config.AttributeAuthorityConfig;
import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AuthorityKey;
import de.tuberlin.tfdacmacs.lib.authority.AttributeAuthorityPublicKeyRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthorityClient {

    private final CAClient caClient;
    private final AttributeAuthorityConfig attributeAuthorityConfig;

    public void uploadAuthorityPublicKey(@NonNull AuthorityKey.Public authorityPublicKey) {
        caClient.updateAuthorityPublicKey(
                attributeAuthorityConfig.getId(),
                new AttributeAuthorityPublicKeyRequest(
                        ElementConverter.convert(authorityPublicKey.getKey())
                )
        );
    }
}
