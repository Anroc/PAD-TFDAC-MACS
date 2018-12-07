package de.tuberlin.tfdacmacs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class CentralServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CentralServerApplication.class, args);
    }
}
