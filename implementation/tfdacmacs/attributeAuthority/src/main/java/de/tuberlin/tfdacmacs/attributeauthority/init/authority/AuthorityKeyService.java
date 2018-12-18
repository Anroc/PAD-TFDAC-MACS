package de.tuberlin.tfdacmacs.attributeauthority.init.authority;

import de.tuberlin.tfdacmacs.attributeauthority.init.authority.events.AuthorityKeyCreatedEvent;
import de.tuberlin.tfdacmacs.attributeauthority.init.gpp.events.GPPReceivedEvent;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.AuthorityKeyGenerator;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.AuthorityKey;
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
        this.authorityKey = authorityKeyGenerator.generate(gppReceivedEvent.getSource());
        return new AuthorityKeyCreatedEvent(this.authorityKey);
    }

    public AuthorityKey.Private getPrivateKey() {
        return authorityKey.getPrivateKey();
    }

    public AuthorityKey.Public getPublicKey() {
        return authorityKey.getPublicKey();
    }


}
