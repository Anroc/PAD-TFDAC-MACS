package de.tuberlin.tfdacmacs.crypto.pairing.data;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class CipherText {

    private final String id = UUID.randomUUID().toString();

    private final Element c1;
    private final Element c2;
    private final Element c3;

    private final Set<String> accessPolicy;
    private final String ownerId;

    private final String encryptedMessage;

    public CipherText(Element c1, Element c2, Element c3, Set<String> accessPolicy, String encryptedMessage) {
        this.c1 = c1.getImmutable();
        this.c2 = c2.getImmutable();
        this.c3 = c3.getImmutable();
        this.accessPolicy = accessPolicy;
        this.ownerId = null;
        this.encryptedMessage = encryptedMessage;
    }

    public boolean isTwoFactorSecured() {
        return this.ownerId != null;
    }

}
