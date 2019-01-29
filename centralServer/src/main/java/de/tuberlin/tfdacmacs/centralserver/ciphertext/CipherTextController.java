package de.tuberlin.tfdacmacs.centralserver.ciphertext;

import de.tuberlin.tfdacmacs.centralserver.ciphertext.data.dto.CipherTextDTO;
import de.tuberlin.tfdacmacs.centralserver.security.AuthenticationFacade;
import de.tuberlin.tfdacmacs.lib.exceptions.NotFoundException;
import de.tuberlin.tfdacmacs.lib.gpp.GlobalPublicParameterProvider;
import it.unisa.dia.gas.jpbc.Field;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ciphertexts")
@RequiredArgsConstructor
public class CipherTextController {

    private final CipherTextService cipherTextService;
    private final GlobalPublicParameterProvider globalPublicParameterProvider;
    private final AuthenticationFacade authenticationFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_USER')")
    public CipherTextDTO createCipherText(@Valid @RequestBody CipherTextDTO cipherTextDTO) {
        Field g1 = globalPublicParameterProvider.getGlobalPublicParameter().getPairing().getG1();
        cipherTextService.insert(cipherTextDTO.toCipherTextEntity(g1));
        return cipherTextDTO;
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public List<CipherTextDTO> getCipherTexts(@RequestParam(value = "attrId", defaultValue = "") String query) {
        if(query.isEmpty()) {
            return cipherTextService.findAll().stream()
                    .map(CipherTextDTO::from)
                    .collect(Collectors.toList());
        } else {
            List<String> attributeIds = Arrays.asList(query.split("\\+"));
            return cipherTextService.findAllByPolicyContaining(attributeIds)
                    .stream()
                    .map(CipherTextDTO::from)
                    .collect(Collectors.toList());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public CipherTextDTO getCipherText(@PathVariable("id") String id) {
        return cipherTextService.findCipherText(id)
                .map(CipherTextDTO::from)
                .orElseThrow(() -> new NotFoundException(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public CipherTextDTO updateCipherText(@PathVariable("id") String id) {
        // TODO: user authentication facade to check the data owner id against the request id
        String userId = authenticationFacade.getId();
        return null;
    }
}
