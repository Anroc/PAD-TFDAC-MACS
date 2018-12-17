package de.tuberlin.tfdacmacs.attributeauthority.init.authority;

import de.tuberlin.tfdacmacs.attributeauthority.init.authority.events.AuthorityKeyCreatedEvent;
import de.tuberlin.tfdacmacs.attributeauthority.init.gpp.events.GPPReceivedEvent;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.AuthorityKeyGenerator;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.AuthorityKey;
import it.unisa.dia.gas.jpbc.Element;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorityKeyService {

    private final AuthorityKeyGenerator authorityKeyGenerator;

    private AuthorityKey authorityKey;

    @EventListener(GPPReceivedEvent.class)
    public AuthorityKeyCreatedEvent setup(GPPReceivedEvent gppReceivedEvent) {
        this.authorityKey = authorityKeyGenerator.generateAuthorityKey(gppReceivedEvent.getSource());
        return new AuthorityKeyCreatedEvent(this.authorityKey);
    }

    public Element getPrivateKey() {
        return authorityKey.getPrivateKey().getKey();
    }

    public Element getPublicKey() {
        return authorityKey.getPublicKey().getKey();
    }


}
