package de.tuberlin.tfdacmacs.crypto.pairing.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DNFAccessPolicy extends AccessPolicyElement {

    private List<AndAccessPolicy> andAccessPolicies = new ArrayList<>();

    @Override
    public void put(@NonNull AccessPolicyElement accessPolicyElement) {
        this.andAccessPolicies.add((AndAccessPolicy) accessPolicyElement);
    }
}
