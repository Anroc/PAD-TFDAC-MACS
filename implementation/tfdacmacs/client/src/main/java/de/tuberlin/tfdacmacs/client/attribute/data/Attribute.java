package de.tuberlin.tfdacmacs.client.attribute.data;

import it.unisa.dia.gas.jpbc.Element;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Attribute {

    private final String id;
    private final Element key;
}
