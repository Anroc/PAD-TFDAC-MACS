package de.tuberlin.tfdacmacs.attributeauthority.attributes;

import de.tuberlin.tfdacmacs.attributeauthority.attributes.data.dto.AttributeCreationRequest;
import de.tuberlin.tfdacmacs.basics.attributes.data.Attribute;
import de.tuberlin.tfdacmacs.basics.attributes.data.dto.PublicAttributeResponse;
import de.tuberlin.tfdacmacs.basics.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
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
    public PublicAttributeResponse createAttribute(
            @Valid @RequestBody AttributeCreationRequest attributeCreationRequest) {
        Attribute attribute = attributeService.createAttribute(
                attributeCreationRequest.getName(),
                attributeCreationRequest.getType(),
                attributeCreationRequest.getValues()
        );

        return PublicAttributeResponse.from(attribute);
    }
}
