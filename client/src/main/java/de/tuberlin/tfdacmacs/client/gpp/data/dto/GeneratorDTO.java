package de.tuberlin.tfdacmacs.client.gpp.data.dto;

import de.tuberlin.tfdacmacs.crypto.pairing.converter.ElementConverter;
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

    public Element toElement(@NonNull Field fieldG1) {
        return ElementConverter.convert(g, fieldG1);
    }
}
