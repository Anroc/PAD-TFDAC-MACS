package de.tuberlin.tfdacmacs.client.attribute.data;

import it.unisa.dia.gas.jpbc.Element;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attribute {

    @NotBlank
    private String id;
    @NotNull
    private Element key;

}
