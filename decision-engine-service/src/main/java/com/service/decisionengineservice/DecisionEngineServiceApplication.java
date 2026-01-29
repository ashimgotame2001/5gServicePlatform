package com.service.decisionengineservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;



@SpringBootApplication(scanBasePackages = {"com.service.decisionengineservice", "com.service.shared"})
@ComponentScan(basePackages = {"com.service.decisionengineservice", "com.service.shared"})
@EntityScan(basePackages = {"com.service.decisionengineservice", "com.service.shared"})
@EnableJpaRepositories(basePackages = {"com.service.decisionengineservice", "com.service.shared"})
@EnableMongoRepositories(basePackages = {"com.service.decisionengineservice", "com.service.shared"})

@EnableScheduling
@EnableAsync
public class DecisionEngineServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DecisionEngineServiceApplication.class, args);
    }

}
