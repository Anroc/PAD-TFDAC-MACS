package de.tuberlin.tfdacmacs.attributeauthority.feign;

import de.tuberlin.tfdacmacs.basics.certificate.data.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.basics.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.basics.user.data.dto.UserCreationRequest;
import de.tuberlin.tfdacmacs.basics.user.data.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient("CentralServer")
public interface CAClient {

    @GetMapping("/gpp")
    GlobalPublicParameterDTO getGPP();

    @PostMapping("/users")
    UserResponse createUser(UserCreationRequest userCreationRequest);

    @GetMapping("/certificates/root")
    CertificateResponse getCentralAuthorityCertificate();

}
