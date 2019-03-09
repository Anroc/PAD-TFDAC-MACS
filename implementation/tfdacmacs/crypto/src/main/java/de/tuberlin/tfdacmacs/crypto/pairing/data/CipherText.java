package de.tuberlin.tfdacmacs.crypto.pairing.data;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class CipherText {

    private final String id;

    private final Element c1;
    private final Element c2;
    private final Element c3;

    private final Set<VersionedID> accessPolicy;
    private final VersionedID ownerId;

    private final String fileId;

    public CipherText(Element c1, Element c2, Element c3, Set<VersionedID> accessPolicy, VersionedID ownerId, String fileId) {
        this(UUID.randomUUID().toString(), c1, c2, c3, accessPolicy, ownerId, fileId);
    }

    public boolean isTwoFactorSecured() {
        return this.ownerId != null;
    }

}
