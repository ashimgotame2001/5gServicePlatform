package com.service.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = {"com.service.authservice", "com.service.shared"})
@ComponentScan(basePackages = {"com.service.authservice", "com.service.shared"})
@EntityScan(basePackages = {"com.service.authservice", "com.service.shared"})
@EnableJpaRepositories(basePackages = {"com.service.authservice", "com.service.shared"})
@EnableMongoRepositories(basePackages = {"com.service.authservice", "com.service.shared"})
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

}
