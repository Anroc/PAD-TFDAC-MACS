package de.tuberlin.tfdacmacs.attributeauthority.gpp.client;

import de.tuberlin.tfdacmacs.basics.gpp.data.dto.GlobalPublicParameterDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("CentralServer")
public interface GPPFeignClient {

    @GetMapping("/gpp")
    GlobalPublicParameterDTO getGPP();

}
