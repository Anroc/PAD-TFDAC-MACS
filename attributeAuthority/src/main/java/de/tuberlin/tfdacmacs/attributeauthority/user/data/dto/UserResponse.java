package de.tuberlin.tfdacmacs.attributeauthority.user.data.dto;

import de.tuberlin.tfdacmacs.attributeauthority.user.data.User;
import de.tuberlin.tfdacmacs.crypto.rsa.converter.KeyConverter;
import de.tuberlin.tfdacmacs.lib.certificate.data.dto.CertificateResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    @NotBlank
    private String id;

    @NotEmpty
    private Set<AttributeValueResponse> attributes;

    private List<CertificateResponse> devices = new ArrayList<>();
    private List<CertificateResponse> unapprovedDevices = new ArrayList<>();

    public static UserResponse from(@NonNull User user) {
        return new UserResponse(
                user.getId(),
                user.getAttributes().stream().map(AttributeValueResponse::from).collect(Collectors.toSet()),
                toCertificateResponseList(user.getDevices()),
                toCertificateResponseList(user.getUnapprovedDevices())
        );


    }

    private static List<CertificateResponse> toCertificateResponseList(@NonNull Map<String, X509Certificate> devices) {
        return devices.entrySet().stream()
                .map(entry -> new CertificateResponse(entry.getKey(), KeyConverter.from(entry.getValue()).toBase64()))
                .collect(Collectors.toList());
    }
}
