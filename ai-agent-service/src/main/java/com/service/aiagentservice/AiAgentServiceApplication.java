package com.service.aiagentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;



@SpringBootApplication(scanBasePackages = {"com.service.aiagentservice", "com.service.shared"})
@ComponentScan(basePackages = {"com.service.aiagentservice", "com.service.shared"})
@EntityScan(basePackages = {"com.service.aiagentservice", "com.service.shared"})
@EnableJpaRepositories(basePackages = {"com.service.aiagentservice", "com.service.shared"})
@EnableMongoRepositories(basePackages = {"com.service.aiagentservice", "com.service.shared"})

@EnableScheduling
@EnableAsync
public class AiAgentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiAgentServiceApplication.class, args);
    }

}
