package de.tuberlin.tfdacmacs.basics.gpp.data.dto;

import de.tuberlin.tfdacmacs.basics.utils.ElementConverter;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class GeneratorDTO {

    @NotBlank
    private String g;

    public static GeneratorDTO from(@NonNull Element g) {
        GeneratorDTO generatorDTO = new GeneratorDTO();
        generatorDTO.setG(ElementConverter.convert(g));
        return generatorDTO;
    }

    public Element toElement(@NonNull Field fieldG1) {
        return ElementConverter.convert(g, fieldG1);
    }
}
