package de.tuberlin.tfdacmacs.basics.crypto.pairing.data;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class CipherText {

    private final String id = UUID.randomUUID().toString();

    private final Element c1;
    private final Element c2;
    private final Element c3;

    private final AndAccessPolicy accessPolicy;
    private final String ownerId;

    private final String encryptedMessage;

    public CipherText(Element c1, Element c2, Element c3, AndAccessPolicy accessPolicy, String encryptedMessage) {
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        this.accessPolicy = accessPolicy;
        this.ownerId = null;
        this.encryptedMessage = encryptedMessage;
    }

    public boolean isTwoFactorSecured() {
        return this.ownerId == null;
    }

}
