package de.tuberlin.tfdacmacs.csp.ciphertext;

import de.tuberlin.tfdacmacs.csp.ciphertext.data.dto.CipherTextDTO;
import de.tuberlin.tfdacmacs.lib.exceptions.NotFoundException;
import de.tuberlin.tfdacmacs.lib.gpp.GlobalPublicParameterProvider;
import it.unisa.dia.gas.jpbc.Field;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/ciphertexts")
@RequiredArgsConstructor
public class CipherTextController {

    private final CipherTextService cipherTextService;
    private final GlobalPublicParameterProvider globalPublicParameterProvider;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CipherTextDTO createCipherText(@Valid @RequestBody CipherTextDTO cipherTextDTO) {
        Field g1 = globalPublicParameterProvider.getGlobalPublicParameter().getPairing().getG1();
        cipherTextService.insert(cipherTextDTO.toCipherTextEntity(g1));
        return cipherTextDTO;
    }

    @GetMapping("/{id}")
    public CipherTextDTO getCipherText(@PathVariable("id") String id) {
        return cipherTextService.findCipherText(id)
                .map(CipherTextDTO::from)
                .orElseThrow(() -> new NotFoundException(id));
    }
}
