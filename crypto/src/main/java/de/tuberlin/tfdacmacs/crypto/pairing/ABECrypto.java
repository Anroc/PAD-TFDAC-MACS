package de.tuberlin.tfdacmacs.crypto.pairing;

import de.tuberlin.tfdacmacs.crypto.pairing.data.CipherText;
import de.tuberlin.tfdacmacs.crypto.pairing.data.VersionedID;
import de.tuberlin.tfdacmacs.crypto.pairing.exceptions.AccessPolicyNotSatisfiedException;
import de.tuberlin.tfdacmacs.crypto.pairing.exceptions.VersionMismatchException;
import it.unisa.dia.gas.jpbc.Element;
import lombok.NonNull;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ABECrypto {

    protected Element mulOrDefault(Element target, @NonNull Element multiplier) {
        if(target == null) {
            return multiplier;
        } else {
            return target.mul(multiplier);
        }
    }

    protected <T> Set<T> findSatisfingSubSet(CipherText cipherText, Set<T> secrets, Function<T, VersionedID> versionedIdReference) {
        Set<T> satisfyingSubSet = getSatisfyingSubSet(cipherText.getAccessPolicy(), secrets, versionedIdReference, true);
        if(satisfyingSubSet.size() != cipherText.getAccessPolicy().size()) {
            satisfyingSubSet = getSatisfyingSubSet(cipherText.getAccessPolicy(), secrets, versionedIdReference, false);


            if(satisfyingSubSet.size() == cipherText.getAccessPolicy().size()) {
                String message = String.format("Policy %s could not be fulfilled by %s. Version mismatch.",
                        cipherText.getAccessPolicy(),
                        secrets.stream().map(versionedIdReference).collect(Collectors.toSet())
                );
                throw new VersionMismatchException(message);
            } else {
                String message = String.format("Policy %s could not be fulfilled by %s. Missing %d attribute keys.",
                        cipherText.getAccessPolicy(),
                        secrets.stream().map(versionedIdReference).collect(Collectors.toSet()),
                        cipherText.getAccessPolicy().size() - satisfyingSubSet.size()
                );
                throw new AccessPolicyNotSatisfiedException(message);
            }
        }

        return satisfyingSubSet;
    }

    private <T> Set<T> getSatisfyingSubSet(Set<VersionedID> accessPolicy, Set<T> secrets, Function<T, VersionedID> versionedIdReference, boolean exactMatch) {
        if(exactMatch) {
            return secrets.stream()
                    .filter(secret -> accessPolicy.contains(versionedIdReference.apply(secret)))
                    .collect(Collectors.toSet());
        } else {
            Set<String> collect = accessPolicy.stream().map(VersionedID::getId).collect(Collectors.toSet());

            return secrets.stream()
                    .filter(secret -> collect.contains(versionedIdReference.apply(secret).getId()))
                    .collect(Collectors.toSet());
        }
    }
}
