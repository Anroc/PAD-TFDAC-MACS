package de.tuberlin.tfdacmacs.client.attribute;

import de.tuberlin.tfdacmacs.client.attribute.data.Attribute;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.Set;

@ShellComponent
@RequiredArgsConstructor
public class AttributeCommand {

    private final AttributeService attributeService;

    @ShellMethod("List attributes")
    public void list() {
        Set<Attribute> attributes = attributeService.getAttributes();
        if (attributes == null) {
            System.out.println("No attributes registered.");
        } else {
            attributes.forEach(
                    attribute -> System.out.println(String.format("%s:\t\t%s", attribute.getId(), attribute.getKey().toBigInteger()))
            );
        }
    }
}
