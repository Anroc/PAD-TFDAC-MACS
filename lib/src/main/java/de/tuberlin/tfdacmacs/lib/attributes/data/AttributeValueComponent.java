package de.tuberlin.tfdacmacs.lib.attributes.data;

import it.unisa.dia.gas.jpbc.Element;

public interface AttributeValueComponent<T> {

    T getValue();

    Element getPublicKeyComponent();
}
