package com.service.connectivityservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = {"com.service.connectivityservice", "com.service.shared"})
@ComponentScan(basePackages = {"com.service.connectivityservice", "com.service.shared"})
@EntityScan(basePackages = {"com.service.connectivityservice", "com.service.shared"})
@EnableJpaRepositories(basePackages = {"com.service.connectivityservice", "com.service.shared"})
@EnableMongoRepositories(basePackages = {"com.service.connectivityservice", "com.service.shared"})
public class ConnectivityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConnectivityServiceApplication.class, args);
    }

}
