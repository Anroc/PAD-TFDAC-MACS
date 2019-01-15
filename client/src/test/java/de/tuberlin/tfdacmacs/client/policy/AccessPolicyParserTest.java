package de.tuberlin.tfdacmacs.client.policy;

import org.junit.Test;

public class AccessPolicyParserTest {

    private final AccessPolicyParser parser = new AccessPolicyParser();

    @Test
    public void parse() {
        parser.parse("a:0 or b:qwe");

    }
}
