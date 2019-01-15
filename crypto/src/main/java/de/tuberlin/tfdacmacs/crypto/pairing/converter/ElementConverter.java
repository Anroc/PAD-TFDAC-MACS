package de.tuberlin.tfdacmacs.crypto.pairing.converter;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;

import java.util.Base64;

public class ElementConverter {

    public static String convert(Element element) {
        if(element == null) {
            return null;
        }
        return new String(
                Base64.getEncoder().encode(element.toBytes())
        );
    }

    public static Element convert(String base64, Field field) {
        if(base64 == null) {
            return null;
        }
        return convert(Base64.getDecoder().decode(base64), field);
    }

    public static Element convert(byte[] bytes, Field field) {
        return field.newElementFromBytes(bytes);
    }
}