package de.tuberlin.tfdacmacs.client.rest;

import de.tuberlin.tfdacmacs.client.authority.events.TrustedAuthorityUpdatedEvent;
import de.tuberlin.tfdacmacs.client.authority.exception.NotTrustedAuthorityException;
import de.tuberlin.tfdacmacs.client.rest.error.SignatureInvalidException;
import de.tuberlin.tfdacmacs.crypto.rsa.StringAsymmetricCryptEngine;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SemanticValidator {

    private final Map<String, PublicKey> trustedPublicKeys = new HashMap<>();
    private final StringAsymmetricCryptEngine cryptEngine;

    @EventListener(TrustedAuthorityUpdatedEvent.class)
    public void updateTrustedPublicKeys(TrustedAuthorityUpdatedEvent event) {
        String id = event.getSource().getId();
        X509Certificate certificate = event.getSource().getCertificate();
        trustedPublicKeys.put(id, certificate.getPublicKey());
    }

    private PublicKey getPublicKey(String authorityId) {
        PublicKey publicKey = trustedPublicKeys.get(authorityId);
        if(publicKey == null) {
            throw new NotTrustedAuthorityException(authorityId);
        }
        return publicKey;
    }

    public boolean isTrustedAuthority(@NonNull String authorityId) {
        return trustedPublicKeys.containsKey(authorityId);
    }

    public void verifySignature(@NonNull String content, @NonNull String signature, @NonNull String authorityId) {
        if(! cryptEngine.isSignatureAuthentic(signature, content, getPublicKey(authorityId))) {
            throw new SignatureInvalidException(
                    String.format("Could not verify signature for content [%s], signature [%s] and authorityId [%s]",
                            content,
                            signature,
                            authorityId)
            );
        }
    }
}
