package de.tuberlin.tfdacmacs.client.encrypt;

import de.tuberlin.tfdacmacs.crypto.pairing.policy.AccessPolicyParser;
import de.tuberlin.tfdacmacs.crypto.pairing.policy.AttributeValueKeyProvider;
import de.tuberlin.tfdacmacs.crypto.pairing.policy.AuthorityKeyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class PolicyParserBeanFactory {

    private final AttributeValueKeyProvider attributeValueKeyProvider;
    private final AuthorityKeyProvider authorityKeyProvider;

    @Bean
    public AccessPolicyParser accessPolicyParser() {
        return new AccessPolicyParser(attributeValueKeyProvider, authorityKeyProvider);
    }
}
