package de.tuberlin.tfdacmacs.centralserver.twofactorkey;

import de.tuberlin.tfdacmacs.centralserver.security.AuthenticationFacade;
import de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.EncryptedTwoFactorKey;
import de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.dto.TwoFactorKeyRequest;
import de.tuberlin.tfdacmacs.centralserver.twofactorkey.data.dto.TwoFactorKeyResponse;
import de.tuberlin.tfdacmacs.lib.exceptions.NotFoundException;
import de.tuberlin.tfdacmacs.lib.exceptions.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/two-factor-keys")
@RequiredArgsConstructor
public class TwoFactorKeyController {

    private final TwoFactorKeyService twoFactorKeyService;
    private final AuthenticationFacade authenticationFacade;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @ResponseStatus(HttpStatus.CREATED)
    public TwoFactorKeyResponse create(@Valid @RequestBody TwoFactorKeyRequest twoFactorKeyRequest) {
        EncryptedTwoFactorKey encryptedTwoFactorKey = new EncryptedTwoFactorKey(
                twoFactorKeyRequest.getUserId(),
                authenticationFacade.getId(),
                twoFactorKeyRequest.getEncryptedTwoFactorKey());

        twoFactorKeyService.insert(encryptedTwoFactorKey);

        return TwoFactorKeyResponse.from(encryptedTwoFactorKey);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public List<TwoFactorKeyResponse> getAll() {
        return twoFactorKeyService.findByUserId(authenticationFacade.getId())
                .map(TwoFactorKeyResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public TwoFactorKeyResponse get(@NotBlank @PathVariable("id") String id) {
        EncryptedTwoFactorKey encryptedTwoFactorKey = twoFactorKeyService.findTwoFactorKey(id)
                .orElseThrow(() -> new NotFoundException(id));

        if(! isRequestingOwner(encryptedTwoFactorKey) && ! isRequestingUser(encryptedTwoFactorKey)) {
            throw new ServiceException("User is not allowed to access this two factor object.", HttpStatus.FORBIDDEN);
        }

        return TwoFactorKeyResponse.from(encryptedTwoFactorKey);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public TwoFactorKeyResponse update(@NotBlank @PathVariable("id") String id, @Valid @RequestBody TwoFactorKeyRequest twoFactorKeyRequest) {
        EncryptedTwoFactorKey encryptedTwoFactorKey = twoFactorKeyService.findTwoFactorKey(id)
                .orElseThrow(() -> new NotFoundException(id));

        if(! isRequestingOwner(encryptedTwoFactorKey)) {
            throw new ServiceException("Only the data owner is allowed to update this two factor key.", HttpStatus.FORBIDDEN);
        }

        encryptedTwoFactorKey.setUserId(twoFactorKeyRequest.getUserId());
        encryptedTwoFactorKey.setEncryptedKey(twoFactorKeyRequest.getEncryptedTwoFactorKey());
        twoFactorKeyService.update(encryptedTwoFactorKey);

        return TwoFactorKeyResponse.from(encryptedTwoFactorKey);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@NotBlank @PathVariable("id") String id) {
        twoFactorKeyService.remove(id);
    }

    private boolean isRequestingUser(EncryptedTwoFactorKey encryptedTwoFactorKey) {
        return authenticationFacade.getId().equals(encryptedTwoFactorKey.getUserId());
    }

    private boolean isRequestingOwner(EncryptedTwoFactorKey encryptedTwoFactorKey) {
        return authenticationFacade.getId().equals(encryptedTwoFactorKey.getDataOwnerId());
    }

}
