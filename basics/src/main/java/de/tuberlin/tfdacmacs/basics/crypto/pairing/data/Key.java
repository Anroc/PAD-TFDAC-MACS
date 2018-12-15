package de.tuberlin.tfdacmacs.basics.crypto.pairing.data;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
public class Key {

    @NotNull
    private final Element privateKey;
    @NotNull
    private final Element publicKey;

    public Key(@NotNull Element privateKey, @NotNull Element publicKey) {
        this.privateKey = privateKey.getImmutable();
        this.publicKey = publicKey.getImmutable();
    }
}
