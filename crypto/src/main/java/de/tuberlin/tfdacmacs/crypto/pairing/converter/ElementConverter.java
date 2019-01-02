package de.tuberlin.tfdacmacs.crypto.pairing.converter;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;

import java.util.Base64;

public class ElementConverter {

    public static String convert(Element element) {
        return new String(
                Base64.getEncoder().encode(element.toBytes())
        );
    }

    public static Element convert(String base64, Field field) {
        return field.newElementFromBytes(Base64.getDecoder().decode(base64));
    }
}
