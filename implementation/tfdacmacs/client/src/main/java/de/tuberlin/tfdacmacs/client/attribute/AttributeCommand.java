package de.tuberlin.tfdacmacs.client.attribute;

import de.tuberlin.tfdacmacs.client.attribute.data.Attribute;
import de.tuberlin.tfdacmacs.client.config.StandardStreams;
import de.tuberlin.tfdacmacs.client.rest.session.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.Set;

@ShellComponent
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class AttributeCommand {

    private final AttributeService attributeService;
    private final Session session;
    private final StandardStreams standardStreams;

    @ShellMethod(value = "List attributes", key = "attributes list")
    public void list() {
        Set<Attribute> attributes = attributeService.getAttributes();
        if (attributes == null) {
            standardStreams.out("No attributes registered.");
        } else {
            attributes.stream()
                    .map(Attribute::getId)
                    .sorted()
                    .map(id -> id.split(":"))
                    .forEach(
                        attribute -> standardStreams.out(String.format("%s\t%s", attribute[0], attribute[1]))
                    );
        }
    }

    @ShellMethod(value = "update attributes", key = "attributes update")
    public void update() {
        attributeService.retrieveAttributesForUser(session.getEmail(), session.getCertificate().getId());
    }
}
