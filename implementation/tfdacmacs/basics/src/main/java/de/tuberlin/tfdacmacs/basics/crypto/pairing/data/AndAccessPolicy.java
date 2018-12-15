package de.tuberlin.tfdacmacs.basics.crypto.pairing.data;

import it.unisa.dia.gas.jpbc.Element;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
public class AndAccessPolicy {

    private Set<AccessPolicyElement> accessPolicyElements;

    public Map<Element, Set<Element>> groupByAttributeAuthority() {
        Map<Element, Set<Element>> map = new HashMap<>();

        for(AccessPolicyElement accessPolicyElement : accessPolicyElements) {
            if(! map.containsKey(accessPolicyElement.getAuthorityPublicKey())) {
                map.put(accessPolicyElement.getAuthorityPublicKey(), new HashSet<>());
            }

            map.get(accessPolicyElement.getAuthorityPublicKey()).add(accessPolicyElement.getAttributePublicKey());
        }

        return map;
    }
}
