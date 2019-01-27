package de.tuberlin.tfdacmacs.attributeauthority.attribute;

import de.tuberlin.tfdacmacs.attributeauthority.attribute.data.dto.AttributeCreationRequest;
import de.tuberlin.tfdacmacs.lib.attributes.data.Attribute;
import de.tuberlin.tfdacmacs.lib.attributes.data.AttributeType;
import de.tuberlin.tfdacmacs.lib.attributes.data.dto.PublicAttributeResponse;
import de.tuberlin.tfdacmacs.lib.exceptions.BadRequestException;
import de.tuberlin.tfdacmacs.lib.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/attributes")
public class AttributeController {

    private final AttributeService attributeService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<PublicAttributeResponse> getAttributes() {
        return attributeService.findAllAttributes()
                .stream()
                .map(PublicAttributeResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public PublicAttributeResponse getAttribute(@PathVariable("id") String attributeId) {
        return attributeService.findAttribute(attributeId)
                .map(PublicAttributeResponse::from)
                .orElseThrow(() -> new NotFoundException(attributeId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public PublicAttributeResponse createAttribute(
            @Valid @RequestBody AttributeCreationRequest attributeCreationRequest) {

        validateAttributeName(attributeCreationRequest.getName());
        validateAttributeValuesForType(attributeCreationRequest.getType(), attributeCreationRequest.getValues());


        Attribute attribute = attributeService.createAttribute(
                attributeCreationRequest.getName(),
                attributeCreationRequest.getType(),
                attributeCreationRequest.getValues()
        );

        return PublicAttributeResponse.from(attribute);
    }

    private void validateAttributeValuesForType(AttributeType type, List<?> values) {
        if (type != AttributeType.BOOLEAN && values.isEmpty()) {
            throw new BadRequestException("Field values shell not be empty on non boolean attribute.");
        }

        for (Object value : values) {
            if (!type.matchesType(value)) {
                throw new BadRequestException("%s does not match the given value '%s'", type, value);
            }

            if(value instanceof String && ((String) value).contains(":")) {
                throw new BadRequestException("Invalid characters ':' in '%s'.", value);
            }
        }
    }

    private void validateAttributeName(String value) {
        if(value.contains(".") || value.contains(":")) {
            throw new BadRequestException("Invalid characters in '%s'.", value);
        }
    }
}
