package de.tuberlin.tfdacmacs.client.policy;

import de.tuberlin.tfdacmacs.crypto.pairing.data.DNFAccessPolicy;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AuthorityKey;
import de.tuberlin.tfdacmacs.crypto.pairing.policy.AccessPolicyParser;
import de.tuberlin.tfdacmacs.crypto.pairing.policy.AttributeValueKeyProvider;
import de.tuberlin.tfdacmacs.crypto.pairing.policy.AuthorityKeyProvider;
import it.unisa.dia.gas.jpbc.Element;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class AccessPolicyParserTest {

    private final AttributeValueKeyProvider attributeService = mock(AttributeValueKeyProvider.class);
    private final AuthorityKeyProvider authorityService = mock(AuthorityKeyProvider.class);

    private final AccessPolicyParser parser = new AccessPolicyParser(attributeService, authorityService);

    @Before
    public void mockServices() {
        Element element = mock(Element.class);

        doReturn(new AttributeValueKey.Public<>(element, "someId", 0L))
                .when(attributeService)
                .getAttributeValuePublicKey(anyString());

        doReturn(new AuthorityKey.Public<>(element, 0L))
                .when(authorityService)
                .getAuthorityPublicKey("aa.tu-berlin.de");
    }

    @Test
    public void parse_passes_forNestedStructure() {
        DNFAccessPolicy dnfAccessPolicy = parser
                .parse("(aa.tu-berlin.de.age:24 and aa.tu-berlin.de.role:student) or aa.tu-berlin.de.role:professor");

        assertThat(dnfAccessPolicy.getAndAccessPolicies()).hasSize(2);
        assertThat(dnfAccessPolicy.getAndAccessPolicies().get(0).getAttributePolicyElements()).hasSize(2);
        assertThat(dnfAccessPolicy.getAndAccessPolicies().get(1).getAttributePolicyElements()).hasSize(1);
    }

    @Test
    public void parse_passes_forOnlyAnds() {
        DNFAccessPolicy dnfAccessPolicy = parser
                .parse("(aa.tu-berlin.de.age:24 and aa.tu-berlin.de.role:student)");

        assertThat(dnfAccessPolicy.getAndAccessPolicies()).hasSize(1);
        assertThat(dnfAccessPolicy.getAndAccessPolicies().get(0).getAttributePolicyElements()).hasSize(2);
    }

    @Test
    public void parse_passes_forOnlyOrs() {
        DNFAccessPolicy dnfAccessPolicy = parser
                .parse("(aa.tu-berlin.de.age:24 or aa.tu-berlin.de.role:student)");

        assertThat(dnfAccessPolicy.getAndAccessPolicies()).hasSize(2);
        assertThat(dnfAccessPolicy.getAndAccessPolicies().get(0).getAttributePolicyElements()).hasSize(1);
        assertThat(dnfAccessPolicy.getAndAccessPolicies().get(1).getAttributePolicyElements()).hasSize(1);
    }

    @Test
    public void parse_passes_booleanAttributes() {
        DNFAccessPolicy dnfAccessPolicy = parser
                .parse("aa.tu-berlin.de.gender:true");

        assertThat(dnfAccessPolicy.getAndAccessPolicies()).hasSize(1);
        assertThat(dnfAccessPolicy.getAndAccessPolicies().get(0).getAttributePolicyElements()).hasSize(1);
    }

    @Test
    public void parser_fails_onSyntaxError() {
        assertThatExceptionOfType(ParseCancellationException.class)
                .isThrownBy(
                        () -> parser.parse("or asd:asd or")
                );
    }
}
