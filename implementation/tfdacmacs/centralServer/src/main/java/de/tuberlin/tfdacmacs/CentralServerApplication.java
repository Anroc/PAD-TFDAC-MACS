package de.tuberlin.tfdacmacs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class CentralServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CentralServerApplication.class, args);
    }
}
