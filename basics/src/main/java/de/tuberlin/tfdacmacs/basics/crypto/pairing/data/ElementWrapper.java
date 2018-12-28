package de.tuberlin.tfdacmacs.basics.crypto.pairing.data;

import de.tuberlin.tfdacmacs.basics.crypto.pairing.converter.ElementConverter;
import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;

import java.io.Serializable;

@Data
public class ElementWrapper implements Serializable {

    private Element element;

    private final String serializedElement;

    public ElementWrapper(Element element) {
        this.serializedElement = ElementConverter.convert(element);
    }
}
