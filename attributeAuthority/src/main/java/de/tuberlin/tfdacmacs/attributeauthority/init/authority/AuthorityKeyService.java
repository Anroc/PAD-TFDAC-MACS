package de.tuberlin.tfdacmacs.attributeauthority.init.authority;

import de.tuberlin.tfdacmacs.attributeauthority.init.authority.events.AuthorityKeyCreatedEvent;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.AuthorityKeyGenerator;
import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.AuthorityKey;
import de.tuberlin.tfdacmacs.basics.gpp.events.GlobalPublicParameterChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorityKeyService {

    private final AuthorityKeyGenerator authorityKeyGenerator;

    private AuthorityKey authorityKey;

    @EventListener(GlobalPublicParameterChangedEvent.class)
    public AuthorityKeyCreatedEvent setup(GlobalPublicParameterChangedEvent event) {
        this.authorityKey = authorityKeyGenerator.generate(event.getSource());
        return new AuthorityKeyCreatedEvent(this.authorityKey);
    }

    public AuthorityKey.Private getPrivateKey() {
        return authorityKey.getPrivateKey();
    }

    public AuthorityKey.Public getPublicKey() {
        return authorityKey.getPublicKey();
    }


}
