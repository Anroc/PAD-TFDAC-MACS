package de.tuberlin.tfdacmacs.crypto.benchmark;

import de.tuberlin.tfdacmacs.crypto.pairing.AttributeValueKeyGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.AuthorityKeyGenerator;
import de.tuberlin.tfdacmacs.crypto.pairing.data.GlobalPublicParameter;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AttributeValueKey;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.AuthorityKey;
import de.tuberlin.tfdacmacs.crypto.pairing.policy.AttributeValueKeyProvider;
import de.tuberlin.tfdacmacs.crypto.pairing.policy.AuthorityKeyProvider;
import de.tuberlin.tfdacmacs.crypto.pairing.util.HashGenerator;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class SetupWrapper {

    private final GlobalPublicParameter globalPublicParameter;
    private final AttributeValueKeyGenerator attributeValueKeyGenerator;
    private final String authorityId;

    private final Map<String, AttributeValueKey> createdKeys = new HashMap<>();
    @Getter
    private final AuthorityKey authorityKey;

    public SetupWrapper(GlobalPublicParameter globalPublicParameter, String authorityId) {
        this.globalPublicParameter = globalPublicParameter;
        this.attributeValueKeyGenerator = new AttributeValueKeyGenerator(new HashGenerator());
        this.authorityId = authorityId;

        this.authorityKey = new AuthorityKeyGenerator().generate(globalPublicParameter);
    }

    public List<AttributeValueKey> createAttributeValueKeys(int num) {
        List<AttributeValueKey> list = new ArrayList<>();
        for (int i = 0; i <num; i++) {
            list.add(createNewAttributeValueKey());
        }
        return list;
    }

    public String policy() {
        return "(" + StringUtils.collectionToDelimitedString(createdKeys.keySet(), " and ") + ")";
    }

    public AuthorityKey.Private authorityPrivateKey() {
        return authorityKey.getPrivateKey();
    }

    public AttributeValueKeyProvider attributeValueKeyProvider() {
        return (id) -> createdKeys.get(id).getPublicKey();
    }

    public AuthorityKeyProvider authorityKeyProvider() {
        return (id) -> authorityKey.getPublicKey();
    }

    private AttributeValueKey createNewAttributeValueKey() {
        AttributeValueKey attributeValueKey = attributeValueKeyGenerator
                .generateNew(globalPublicParameter, newAttributeValueId());
        this.createdKeys.put(attributeValueKey.getAttributeValueId(), attributeValueKey);
        return attributeValueKey;
    }

    private String newAttributeValueId() {
        String randomId = randomWord();
        String valueId = randomWord();
        return authorityId + "." + randomId + ":" + valueId;
    }

    private String randomWord() {
        Random r = new Random();

        String alphabet = "abcdefghijklmopqstuvwxyz";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            builder.append(alphabet.charAt(r.nextInt(alphabet.length())));
        }
        return builder.toString();
    }

    public List<AttributeValueKey> createdKeys() {
        return this.createdKeys.values().stream().collect(Collectors.toList());
    }

}
