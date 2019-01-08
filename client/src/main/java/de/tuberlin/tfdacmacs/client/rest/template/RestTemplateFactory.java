package de.tuberlin.tfdacmacs.client.rest.template;

import de.tuberlin.tfdacmacs.client.config.ClientConfig;
import de.tuberlin.tfdacmacs.client.keypair.config.KeyStoreConfig;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
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

    private final ClientConfig clientConfig;

    protected RestTemplate plainRestTemplate(@NonNull String rootURL) {
        RestTemplate restTemplate = new RestTemplateBuilder().rootUri(rootURL).build();
        return restTemplate;
    }

    @Bean
    protected RestTemplate sslRestTemplate() {
        return buildRestTemplate(null);
    }

    public RestTemplate updateForMutalAuthentication(@NonNull RestTemplate restTemplate) {
        try {
            HttpClient httpClient = buildHttpClient(clientConfig.getP12Certificate());
            ((HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory()).setHttpClient(httpClient);
            return restTemplate;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private RestTemplate buildRestTemplate(KeyStoreConfig p12Certificate) {
        try {
            HttpClient httpClient = buildHttpClient(p12Certificate);
            RestTemplate restTemplate = plainRestTemplate(clientConfig.getCaRootUrl());
            ((HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory()).setHttpClient(httpClient);
            return restTemplate;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HttpClient buildHttpClient(KeyStoreConfig p12Certificate)
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
                    clientConfig.locateResource(p12Certificate.getLocation()),
                    p12Certificate.getKeyStorePassword().toCharArray(),
                    p12Certificate.getKeyPassword().toCharArray()
            );
        }
        SSLContext sslContext = sslContextBuilder.build();
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
        return HttpClients.custom().setSSLSocketFactory(socketFactory).build();
    }

}
