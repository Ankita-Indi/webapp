package com.example.healthcheckapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
//@ComponentScan(basePackages = {"com.example.healthcheckapi"})
//@RestController
public class HealthCheckApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthCheckApiApplication.class, args);
    }

}
