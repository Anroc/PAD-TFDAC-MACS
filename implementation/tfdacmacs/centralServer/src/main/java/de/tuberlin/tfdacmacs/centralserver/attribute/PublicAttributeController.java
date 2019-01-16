package de.tuberlin.tfdacmacs.centralserver.attribute;

import de.tuberlin.tfdacmacs.centralserver.security.AuthenticationFacade;
import de.tuberlin.tfdacmacs.lib.attributes.data.PublicAttribute;
import de.tuberlin.tfdacmacs.lib.attributes.data.PublicAttributeValue;
import de.tuberlin.tfdacmacs.lib.attributes.data.dto.AttributeCreationRequest;
import de.tuberlin.tfdacmacs.lib.attributes.data.dto.AttributeValueCreationRequest;
import de.tuberlin.tfdacmacs.lib.attributes.data.dto.PublicAttributeResponse;
import de.tuberlin.tfdacmacs.lib.attributes.data.dto.PublicAttributeValueResponse;
import de.tuberlin.tfdacmacs.lib.exceptions.NotFoundException;
import de.tuberlin.tfdacmacs.lib.exceptions.ServiceException;
import de.tuberlin.tfdacmacs.lib.gpp.GlobalPublicParameterProvider;
import it.unisa.dia.gas.jpbc.Field;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/attributes")
@RequiredArgsConstructor
public class PublicAttributeController {

    private final PublicAttributeService publicAttributeService;
    private final GlobalPublicParameterProvider gppProvider;
    private final AuthenticationFacade authenticationFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_AUTHORITY')")
    public PublicAttributeResponse createAttribute(@Valid @RequestBody AttributeCreationRequest attributeCreationRequest) {
        PublicAttribute publicAttribute = attributeCreationRequest.toAttribute(getG1());

        checkAuthorityAccess(publicAttribute);

        publicAttributeService.insertEntity(
                publicAttribute
        );

        return PublicAttributeResponse.from(publicAttribute);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_AUTHORITY', 'ROLE_ADMIN')")
    public List<PublicAttributeResponse> getAttributes() {
        return publicAttributeService.findAll()
                .stream()
                .map(PublicAttributeResponse::from)
                .collect(Collectors.toList());

    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_AUTHORITY', 'ROLE_ADMIN')")
    public PublicAttributeResponse getAttribute(@PathVariable("id") String id) {
        return publicAttributeService.findEntity(id)
                .map(PublicAttributeResponse::from)
                .orElseThrow(() -> new NotFoundException(id));
    }

    @PostMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_AUTHORITY')")
    public PublicAttributeValueResponse addAttributeValue(@PathVariable("id") String id, @Valid @RequestBody AttributeValueCreationRequest attributeValueCreationRequest) {
        PublicAttribute publicAttribute = publicAttributeService.findEntity(id)
                .orElseThrow(() -> new NotFoundException(id));

        checkAuthorityAccess(publicAttribute);

        PublicAttributeValue publicAttributeValue = attributeValueCreationRequest.toAttributeValue(
                getG1(),
                publicAttribute.getType()
        );

        try {
            publicAttributeService.addValue(publicAttribute, publicAttributeValue);
            return PublicAttributeValueResponse.from(publicAttributeValue);
        } catch(IllegalArgumentException e) {
            throw new ServiceException(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @GetMapping("/{authorityId}/values/{valueId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_AUTHORITY', 'ROLE_ADMIN')")
    public PublicAttributeValueResponse getAttribute(@PathVariable("authorityId") String authorityId, @PathVariable("valueId") String valueId) {
        return publicAttributeService.findEntity(authorityId)
                .map(PublicAttribute::getValues)
                .map(Set::stream)
                .orElseThrow(() -> new NotFoundException(authorityId))
                .filter(attributeValue -> attributeValue.getValue().toString().equals(valueId))
                .findAny()
                .map(PublicAttributeValueResponse::from)
                .orElseThrow(() -> new NotFoundException(valueId));
    }

    public Field getG1() {
        return gppProvider.getGlobalPublicParameter().getPairing().getG1();
    }

    public void checkAuthorityAccess(PublicAttribute publicAttribute) {
        if (!publicAttribute.getAuthorityDomain().equals(authenticationFacade.getId())) {
            throw new ServiceException("Attribute does not belong to this authority.", HttpStatus.FORBIDDEN);
        }
    }
}
