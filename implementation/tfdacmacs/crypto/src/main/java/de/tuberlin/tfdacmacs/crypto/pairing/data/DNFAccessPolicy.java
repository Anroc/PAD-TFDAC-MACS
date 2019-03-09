package de.tuberlin.tfdacmacs.crypto.pairing.data;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DNFAccessPolicy extends AccessPolicyElement {

    private List<AndAccessPolicy> andAccessPolicies = new ArrayList<>();

    @Override
    public void put(@NonNull AccessPolicyElement accessPolicyElement) {
        this.andAccessPolicies.add((AndAccessPolicy) accessPolicyElement);
    }
}
