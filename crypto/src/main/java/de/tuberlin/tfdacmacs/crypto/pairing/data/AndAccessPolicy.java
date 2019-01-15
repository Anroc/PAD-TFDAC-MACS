package de.tuberlin.tfdacmacs.crypto.pairing.data;

import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AuthorityKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AndAccessPolicy extends AccessPolicyElement {

    private Set<AttributePolicyElement> attributePolicyElements = new HashSet<>();

    public Map<AuthorityKey.Public, Set<AttributePolicyElement>> groupByAttributeAuthority() {
        Map<AuthorityKey.Public, Set<AttributePolicyElement>> map = new HashMap<>();

        for(AttributePolicyElement attributePolicyElement : attributePolicyElements) {
            if(! map.containsKey(attributePolicyElement.getAuthorityPublicKey())) {
                map.put(attributePolicyElement.getAuthorityPublicKey(), new HashSet<>());
            }

            map.get(attributePolicyElement.getAuthorityPublicKey()).add(attributePolicyElement);
        }

        return map;
    }

    public boolean contains(@NonNull String attributeValueId) {
        return attributePolicyElements.stream()
                .map(AttributePolicyElement::getAttributeValueId)
                .anyMatch(attributeValueId::equals);
    }

    @Override
    public void put(@NonNull AccessPolicyElement accessPolicyElement) {
        this.attributePolicyElements.add((AttributePolicyElement) accessPolicyElement);
    }
}
