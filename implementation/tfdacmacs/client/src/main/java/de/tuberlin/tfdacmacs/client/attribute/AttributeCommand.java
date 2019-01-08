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
            attributes.stream()
                    .map(Attribute::getId)
                    .sorted()
                    .map(id -> id.split(":"))
                    .forEach(
                        attribute -> System.out.println(String.format("%s\t%s", attribute[0], attribute[1]))
                    );
        }
    }
}
