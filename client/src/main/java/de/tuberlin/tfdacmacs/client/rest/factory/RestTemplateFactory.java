package de.tuberlin.tfdacmacs.client.rest.factory;

import de.tuberlin.tfdacmacs.client.config.ClientConfig;
import de.tuberlin.tfdacmacs.client.keypair.config.KeyStoreConfig;
import de.tuberlin.tfdacmacs.client.register.events.SessionCreatedEvent;
import de.tuberlin.tfdacmacs.client.rest.template.ClientRestTemplate;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

@Component
@RequiredArgsConstructor
public class RestTemplateFactory {

    public static final String CA_REST_TEMPLATE_BEAN_NAME = "caRestTemplate";
    public static final String CSP_REST_TEMPLATE_BEAN_NAME = "cspRestTemplate";
    public static final String AA_REST_TEMPLATE_BEAN_NAME = "aaRestTemplate";

    private final ClientConfig clientConfig;
    private final ApplicationContext applicationContext;

    protected RestTemplate plainRestTemplate(@NonNull String rootURL) {
        RestTemplate restTemplate = new RestTemplateBuilder().rootUri(rootURL).build();
        return restTemplate;
    }

    @Bean(RestTemplateFactory.CA_REST_TEMPLATE_BEAN_NAME)
    protected RestTemplate sslCARestTemplate() {
        return buildRestTemplate(clientConfig.getCaRootUrl(), null);
    }

    @Bean(RestTemplateFactory.CSP_REST_TEMPLATE_BEAN_NAME)
    protected RestTemplate sslCSPRestTemplate() {
        return buildRestTemplate(clientConfig.getCspRootUrl(), null);
    }

    @Bean(RestTemplateFactory.AA_REST_TEMPLATE_BEAN_NAME)
    protected RestTemplate sslAARestTemplate() {
        return buildRestTemplate(clientConfig.getAaRootUrl(), null);
    }

    @EventListener(SessionCreatedEvent.class)
    public void updateForMutualAuthentication(SessionCreatedEvent sessionCreatedEvent) {
        applicationContext.getBeansOfType(ClientRestTemplate.class).values()
                .forEach(clientRestTemplate -> updateRestTemplate(
                        sessionCreatedEvent.getEmail(),
                        clientRestTemplate.getRestTemplate())
                );
    }

    protected RestTemplate updateRestTemplate(@NonNull String email, @NonNull RestTemplate restTemplate) {
        try {
            HttpClient httpClient = buildHttpClient(clientConfig.getP12Certificate(), email);
            ((HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory()).setHttpClient(httpClient);
            return restTemplate;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected RestTemplate buildRestTemplate(String rootUrl, KeyStoreConfig p12Certificate) {
        try {
            HttpClient httpClient = buildHttpClient(p12Certificate, null);
            RestTemplate restTemplate = plainRestTemplate(rootUrl);
            ((HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory()).setHttpClient(httpClient);
            return restTemplate;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HttpClient buildHttpClient(KeyStoreConfig p12Certificate, String email)
            throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException,
            UnrecoverableKeyException, KeyManagementException {
        SSLContextBuilder sslContextBuilder = SSLContexts
                .custom()
                .loadTrustMaterial(
                        clientConfig.locateResource(clientConfig.getTrustStore()),
                        clientConfig.getTrustStorePassword().toCharArray()
                );
        if(p12Certificate != null) {
            sslContextBuilder.loadKeyMaterial(
                    clientConfig.locateResource(p12Certificate.getLocation() + email + ".jks"),
                    p12Certificate.getKeyStorePassword().toCharArray(),
                    p12Certificate.getKeyPassword().toCharArray()
            );
        }
        SSLContext sslContext = sslContextBuilder.build();
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
        return HttpClients.custom().setSSLSocketFactory(socketFactory).build();
    }

}
