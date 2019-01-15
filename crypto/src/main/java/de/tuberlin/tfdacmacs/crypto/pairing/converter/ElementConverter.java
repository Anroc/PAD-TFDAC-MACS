package de.tuberlin.tfdacmacs.crypto.pairing.converter;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import lombok.NonNull;

import java.util.Base64;

public class ElementConverter {

    public static String convert(@NonNull Element element) {
        return new String(
                Base64.getEncoder().encode(element.toBytes())
        );
    }

    public static Element convert(@NonNull String base64, @NonNull Field field) {
        return convert(Base64.getDecoder().decode(base64), field);
    }

    public static Element convert(byte[] bytes, @NonNull Field field) {
        return field.newElementFromBytes(bytes);
    }
}
