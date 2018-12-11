package de.tuberlin.tfdacmacs.attributeauthority.user.data;

import it.unisa.dia.gas.jpbc.Element;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class UserAttributeKey<T> {

    @NotBlank
    private String attributeId;
    @NotNull
    private T value;
    @NotNull
    private Element key;
}
