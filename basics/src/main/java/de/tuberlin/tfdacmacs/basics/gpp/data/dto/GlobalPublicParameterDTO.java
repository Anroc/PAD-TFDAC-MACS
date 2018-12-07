package de.tuberlin.tfdacmacs.basics.gpp.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalPublicParameterDTO {

    @NotNull
    private CurveParameterDTO curveParameter;
    @NotNull
    private GeneratorDTO generator;
}
