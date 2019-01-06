package de.tuberlin.tfdacmacs.dto.attributeauthority.user;

import de.tuberlin.tfdacmacs.dto.attributeauthority.certificate.AaCertificateResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    @NotBlank
    private String id;

    @NotEmpty
    private Set<AttributeValueResponse> attributes;

    private List<AaCertificateResponse> devices = new ArrayList<>();
    private List<AaCertificateResponse> unapprovedDevices = new ArrayList<>();
}
