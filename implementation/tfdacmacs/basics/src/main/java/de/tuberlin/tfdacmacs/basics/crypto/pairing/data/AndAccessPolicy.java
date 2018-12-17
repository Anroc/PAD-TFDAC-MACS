package de.tuberlin.tfdacmacs.basics.crypto.pairing.data;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.data.keys.AuthorityKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
public class AndAccessPolicy {

    private Set<AccessPolicyElement> accessPolicyElements;

    public Map<AuthorityKey.Public, Set<AccessPolicyElement>> groupByAttributeAuthority() {
        Map<AuthorityKey.Public, Set<AccessPolicyElement>> map = new HashMap<>();

        for(AccessPolicyElement accessPolicyElement : accessPolicyElements) {
            if(! map.containsKey(accessPolicyElement.getAuthorityPublicKey())) {
                map.put(accessPolicyElement.getAuthorityPublicKey(), new HashSet<>());
            }

            map.get(accessPolicyElement.getAuthorityPublicKey()).add(accessPolicyElement);
        }

        return map;
    }

    public boolean contains(@NonNull String attributeValueId) {
        return accessPolicyElements.stream()
                .map(AccessPolicyElement::getAttributeValueId)
                .anyMatch(attributeValueId::equals);
    }
}
