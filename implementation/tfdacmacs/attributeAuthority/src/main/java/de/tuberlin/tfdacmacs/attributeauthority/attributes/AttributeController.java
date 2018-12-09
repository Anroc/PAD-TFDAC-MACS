package de.tuberlin.tfdacmacs.attributeauthority.attributes;

import de.tuberlin.tfdacmacs.attributeauthority.attributes.data.dto.AttributeCreationRequest;
import de.tuberlin.tfdacmacs.basics.attributes.data.Attribute;
import de.tuberlin.tfdacmacs.basics.attributes.data.AttributeType;
import de.tuberlin.tfdacmacs.basics.attributes.data.dto.PublicAttributeResponse;
import de.tuberlin.tfdacmacs.basics.exceptions.BadRequestException;
import de.tuberlin.tfdacmacs.basics.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public List<PublicAttributeResponse> getAttributes() {
        return attributeService.findAllAttributes()
                .stream()
                .map(PublicAttributeResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public PublicAttributeResponse getAttribute(@PathVariable("id") String attributeId) {
        return attributeService.findAttribute(attributeId)
                .map(PublicAttributeResponse::from)
                .orElseThrow(() -> new NotFoundException(attributeId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
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
