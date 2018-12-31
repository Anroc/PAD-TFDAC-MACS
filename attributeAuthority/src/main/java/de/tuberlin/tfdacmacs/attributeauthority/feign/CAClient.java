package de.tuberlin.tfdacmacs.attributeauthority.feign;

import de.tuberlin.tfdacmacs.basics.certificate.data.dto.CertificateResponse;
import de.tuberlin.tfdacmacs.basics.gpp.data.dto.GlobalPublicParameterDTO;
import de.tuberlin.tfdacmacs.basics.certificate.data.dto.InitCertificateRequest;
import de.tuberlin.tfdacmacs.basics.certificate.data.dto.CertificatePreparedResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient("CentralServer")
public interface CAClient {

    @GetMapping("/gpp")
    GlobalPublicParameterDTO getGPP();

    @PostMapping("/users")
    CertificatePreparedResponse createUser(InitCertificateRequest initCertificateRequest);

    @GetMapping("/certificates/root")
    CertificateResponse getCentralAuthorityCertificate();

}
