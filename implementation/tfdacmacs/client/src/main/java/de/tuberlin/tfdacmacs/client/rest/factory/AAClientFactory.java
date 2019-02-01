package de.tuberlin.tfdacmacs.client.rest.factory;

import de.tuberlin.tfdacmacs.client.authority.events.TrustedAuthorityUpdatedEvent;
import de.tuberlin.tfdacmacs.client.register.Session;
import de.tuberlin.tfdacmacs.client.rest.template.AAClientTemplate;
import de.tuberlin.tfdacmacs.client.rest.template.RestTemplateFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.support.GenericWebApplicationContext;

@Component
@RequiredArgsConstructor
public class AAClientFactory {

    private final GenericWebApplicationContext context;
    private final RestTemplateFactory restTemplateFactory;
    private final Session session;

    @EventListener(TrustedAuthorityUpdatedEvent.class)
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void createAAClient(TrustedAuthorityUpdatedEvent trustedAuthorityUpdatedEvent) {
        String aid = trustedAuthorityUpdatedEvent.getSource().getId();
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(AAClientTemplate.class);

        RestTemplate restTemplate = restTemplateFactory
                .buildRestTemplate(aid, null);

        if(session.isActive()) {
            restTemplateFactory.updateRestTemplate(session.getEmail(), restTemplate);
        }

        beanDefinitionBuilder.addConstructorArgValue(restTemplate);
        context.registerBeanDefinition(aid, beanDefinitionBuilder.getBeanDefinition());
    }
}
