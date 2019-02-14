package de.tuberlin.tfdacmacs.centralserver.ciphertext;

import de.tuberlin.tfdacmacs.centralserver.ciphertext.data.dto.CipherTextDTO;
import de.tuberlin.tfdacmacs.centralserver.security.AuthenticationFacade;
import de.tuberlin.tfdacmacs.crypto.pairing.data.keys.CipherText2FAUpdateKey;
import de.tuberlin.tfdacmacs.lib.ciphertext.data.dto.TwoFactorCipherTextUpdateRequest;
import de.tuberlin.tfdacmacs.lib.exceptions.NotFoundException;
import de.tuberlin.tfdacmacs.lib.exceptions.ServiceException;
import de.tuberlin.tfdacmacs.lib.gpp.GlobalPublicParameterProvider;
import it.unisa.dia.gas.jpbc.Field;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
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
        Field gt = globalPublicParameterProvider.getGlobalPublicParameter().getPairing().getGT();
        cipherTextService.insert(cipherTextDTO.toCipherTextEntity(g1, gt));
        return cipherTextDTO;
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public List<CipherTextDTO> getCipherTexts(
            @RequestParam(value = "attrIds", defaultValue = "") String query,
            @RequestParam(value = "ownerId", defaultValue = "") String ownerId) {
        if(query.isEmpty() && ownerId.isEmpty()) {
            return findAll();
        } else if (! query.isEmpty() && ownerId.isEmpty()) {
            return findByAttributeIds(query);
        } else if (query.isEmpty() && ! ownerId.isEmpty()) {
            return findByOwnerId(ownerId);
        } else {
            return findByAttributeIds(query)
                    .stream()
                    .filter(ct -> ownerId.equals(ct.getOwnerId()))
                    .collect(Collectors.toList());
        }
    }

    private List<CipherTextDTO> findAll() {
        return cipherTextService.findAll().stream()
                .map(CipherTextDTO::from)
                .collect(Collectors.toList());
    }

    private List<CipherTextDTO> findByOwnerId(String ownerId) {
        return cipherTextService.findAllByOwnerId(ownerId)
                .stream()
                .map(CipherTextDTO::from)
                .collect(Collectors.toList());
    }

    private List<CipherTextDTO> findByAttributeIds(String query) {
        List<String> attributeIds = Arrays.asList(query.split(","));
        return cipherTextService.findAllByPolicyContaining(attributeIds)
                .stream()
                .map(CipherTextDTO::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public CipherTextDTO getCipherText(@PathVariable("id") String id) {
        return cipherTextService.findCipherText(id)
                .map(CipherTextDTO::from)
                .orElseThrow(() -> new NotFoundException(id));
    }

    @PutMapping("/update/2fa")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public List<CipherTextDTO> updateCipherTexts(@RequestBody @Valid TwoFactorCipherTextUpdateRequest twoFactorCipherTextUpdateRequest) {
        if(! twoFactorCipherTextUpdateRequest.getOwnerId().equals(authenticationFacade.getId())) {
            throw new ServiceException("Wrong ownerId.", HttpStatus.FORBIDDEN);
        }

        Set<CipherText2FAUpdateKey> cipherText2FAUpdateKeys = twoFactorCipherTextUpdateRequest
                .toCipherText2FAUpdateKey(
                        globalPublicParameterProvider.getGlobalPublicParameter().getPairing().getG1()
                );

        return cipherTextService.update(twoFactorCipherTextUpdateRequest.getOwnerId(), cipherText2FAUpdateKeys)
                .stream()
                .map(CipherTextDTO::from)
                .collect(Collectors.toList());
    }
}
