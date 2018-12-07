package de.tuberlin.tfdacmacs.basics.gpp.data.dto;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import java.math.BigInteger;
import java.util.Base64;

@Data
@NoArgsConstructor
public class GeneratorDTO {

    @NotBlank
    private String g;

    public static GeneratorDTO from(@NonNull Element g) {
        GeneratorDTO generatorDTO = new GeneratorDTO();
        generatorDTO.setG(new String(Base64.getEncoder().encode(g.toBytes())));
        return generatorDTO;
    }

    public Element toElement(@NonNull Pairing pairing) {
        Element element = pairing.getG1().newElement();
        element.setFromBytes(Base64.getDecoder().decode(this.g));
        return element;
    }
}
