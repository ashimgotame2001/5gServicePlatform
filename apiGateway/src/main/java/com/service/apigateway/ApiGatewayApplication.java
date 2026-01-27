package com.service.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * API Gateway Application
 * Spring Cloud Gateway Server MVC will handle routing automatically
 */
@SpringBootApplication(scanBasePackages = {"com.service.apigateway"})
@ComponentScan(basePackages = {"com.service.apigateway"})
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
