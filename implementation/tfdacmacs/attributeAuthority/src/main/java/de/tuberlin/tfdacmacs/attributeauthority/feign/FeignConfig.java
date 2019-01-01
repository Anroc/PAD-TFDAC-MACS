package de.tuberlin.tfdacmacs.attributeauthority.feign;

import feign.Client;
import feign.Contract;
import feign.Feign;
import feign.Logger;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContexts;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

@Configuration
public class FeignConfig {

    @Bean
    public Feign.Builder feignBuilder(){
        SSLContext sslContext = SSLContexts
                .custom()
                .loadTrustMaterial(ResourceUtils.getFile("classpath:aa-truststore.jks"), "foobar".toCharArray())
                .loadKeyMaterial(
                        ResourceUtils.getFile("classpath:aa-authority-keystore.jks"),
                        "foobar".toCharArray(),
                        "foobar".toCharArray()
                ).build();
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
        SSLSocketFactory sslSocketFactory = new SSL

        Client trustSSLSockets = new Client.Default(socketFactory, null);
        return Feign.builder().client(trustSSLSockets);
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public Contract feignContract() {
        return new feign.Contract.Default();
    }

}
