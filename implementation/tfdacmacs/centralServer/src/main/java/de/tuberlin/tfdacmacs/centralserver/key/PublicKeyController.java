package de.tuberlin.tfdacmacs.centralserver.key;

import de.tuberlin.tfdacmacs.basics.gpp.data.dto.RSAPublicKeyDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/keys")
public class PublicKeyController {

    private final KeyService keyService;

    @GetMapping
    public RSAPublicKeyDTO getPublicKey() {
        return RSAPublicKeyDTO.from(keyService.getPublicKey());
    }
}
