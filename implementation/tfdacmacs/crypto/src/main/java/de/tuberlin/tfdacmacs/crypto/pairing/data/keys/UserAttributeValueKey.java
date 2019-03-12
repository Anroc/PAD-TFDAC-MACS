package de.tuberlin.tfdacmacs.crypto.pairing.data.keys;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserAttributeValueKey extends ElementKey {
    public UserAttributeValueKey(@NonNull Element key, long version) {
        super(key, version);
    }

    public UserAttributeValueKey update(@NonNull UserAttributeValueUpdateKey userAttributeValueUpdateKey) {
        userAttributeValueUpdateKey.checkApplicablilty(this);
        Element newKey = getKey().duplicate().mul(userAttributeValueUpdateKey.getUpdateKey());
        return update(newKey);
    }

    @Override
    public UserAttributeValueKey clone() {
        return new UserAttributeValueKey(
                getKey().duplicate(),
                getVersion()
        );
    }
}
