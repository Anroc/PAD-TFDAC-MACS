package de.tuberlin.tfdacmacs.client.rest.factory;

import de.tuberlin.tfdacmacs.client.authority.events.TrustedAuthorityUpdatedEvent;
import de.tuberlin.tfdacmacs.client.config.ClientConfig;
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

    private final ClientConfig clientConfig;

    @EventListener(TrustedAuthorityUpdatedEvent.class)
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void createAAClient(TrustedAuthorityUpdatedEvent trustedAuthorityUpdatedEvent) {
        String aid = trustedAuthorityUpdatedEvent.getSource().getId();
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(AAClientTemplate.class);

        String url = aid;

        // TODO: just for testing, remove later
        if(aid.equals("aa.tu-berlin.de")) {
            url = clientConfig.getAaRootUrl();
        }

        RestTemplate restTemplate = restTemplateFactory
                .buildRestTemplate(url, null);

        if(session.isActive()) {
            restTemplateFactory.updateRestTemplate(session.getEmail(), restTemplate);
        }

        beanDefinitionBuilder.addConstructorArgValue(restTemplate);
        context.registerBeanDefinition(aid, beanDefinitionBuilder.getBeanDefinition());
    }
}
