package de.tuberlin.tfdacmacs.attributeauthority.user.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceResponse {

    @NotBlank
    private String id;
}
