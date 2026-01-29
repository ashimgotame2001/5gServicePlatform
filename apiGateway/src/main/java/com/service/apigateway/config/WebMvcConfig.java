package com.service.apigateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC Configuration to disable static resource handling for gateway routes
 * This ensures that gateway routes are handled by Spring Cloud Gateway instead of
 * being treated as static resources
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Explicitly disable static resource handling for gateway paths
        // This prevents Spring MVC from trying to serve gateway routes as static resources
        // Gateway routes: /auth/**, /connectivity/**, /identification/**, /location/**, /device/**, /decision-engine/**, /.well-known/**
        registry.setOrder(Integer.MAX_VALUE);
        
        // Only allow static resources for actuator endpoints if needed
        // Gateway routes will be handled by Spring Cloud Gateway
    }
}
