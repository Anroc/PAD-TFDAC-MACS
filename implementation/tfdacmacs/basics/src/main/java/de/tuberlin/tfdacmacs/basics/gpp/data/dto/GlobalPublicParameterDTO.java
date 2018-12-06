package de.tuberlin.tfdacmacs.basics.gpp.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GlobalPublicParameterDTO {

    private CurveParameterDTO curveParameter;
    private GeneratorDTO generator;
}
