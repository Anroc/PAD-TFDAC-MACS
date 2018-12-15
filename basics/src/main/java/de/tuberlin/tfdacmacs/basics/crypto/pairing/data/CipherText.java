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

    private final String accessPolicy;
    private final String ownerId;

    public CipherText(Element c1, Element c2, Element c3, String accessPolicy) {
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        this.accessPolicy = accessPolicy;
        this.ownerId = null;
    }

    public boolean isTwoFactorSecured() {
        return this.ownerId == null;
    }

}
