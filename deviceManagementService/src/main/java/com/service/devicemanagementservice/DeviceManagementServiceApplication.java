package com.service.devicemanagementservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;


@SpringBootApplication(scanBasePackages = {"com.service.devicemanagementservice", "com.service.shared"})
@ComponentScan(basePackages = {"com.service.devicemanagementservice", "com.service.shared"})
@EntityScan(basePackages = {"com.service.devicemanagementservice", "com.service.shared"})
@EnableJpaRepositories(basePackages = {"com.service.devicemanagementservice", "com.service.shared"})
@EnableMongoRepositories(basePackages = {"com.service.devicemanagementservice", "com.service.shared"})
public class DeviceManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeviceManagementServiceApplication.class, args);
    }

}
