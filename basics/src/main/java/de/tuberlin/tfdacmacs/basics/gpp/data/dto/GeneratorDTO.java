package de.tuberlin.tfdacmacs.basics.gpp.data.dto;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import lombok.Data;

import java.math.BigInteger;
import java.util.Base64;

@Data
public class GeneratorDTO {

    private String g;

    public static GeneratorDTO from(Element g) {
        GeneratorDTO generatorDTO = new GeneratorDTO();
        generatorDTO.setG(new String(Base64.getEncoder().encode(g.toBytes())));
        return generatorDTO;
    }

    public Element toElement(Pairing pairing) {
        Element element = pairing.getG1().newElement();
        element.setFromBytes(Base64.getDecoder().decode(this.g));
        return element;
    }
}
