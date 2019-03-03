package de.tuberlin.tfdacmacs.lib.attributes.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttributeValueUpdateRequest {

    @NotBlank
    private String updateKey;
    @Min(1)
    private long version;
    @NotBlank
    private String signature;
}

