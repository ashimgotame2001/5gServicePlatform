package com.service.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

/**
 * Gateway Configuration
 * Programmatically configure routes using Java Routes API
 * This ensures routes are registered before DispatcherServlet processes requests
 */
@Configuration
public class GatewayConfig {

    @Bean
    public RouterFunction<ServerResponse> authRoutes() {
        return route("auth-service")
                .route(org.springframework.web.servlet.function.RequestPredicates.path("/auth/**"), http())
                .before(uri("http://localhost:8085"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> authJwksRoutes() {
        return route("auth-service-jwks")
                .route(org.springframework.web.servlet.function.RequestPredicates.path("/.well-known/**"), http())
                .before(uri("http://localhost:8085"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> connectivityRoutes() {
        return route("connectivity-service")
                .route(org.springframework.web.servlet.function.RequestPredicates.path("/connectivity/**"), http())
                .before(uri("http://localhost:8081"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> identificationRoutes() {
        return route("identification-service")
                .route(org.springframework.web.servlet.function.RequestPredicates.path("/identification/**"), http())
                .before(uri("http://localhost:8082"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> locationRoutes() {
        return route("location-service")
                .route(org.springframework.web.servlet.function.RequestPredicates.path("/location/**"), http())
                .before(uri("http://localhost:8083"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> deviceRoutes() {
        return route("device-management-service")
                .route(org.springframework.web.servlet.function.RequestPredicates.path("/device/**"), http())
                .before(uri("http://localhost:8084"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> aiAgentRoutes() {
        return route("ai-agent-service")
                .route(org.springframework.web.servlet.function.RequestPredicates.path("/ai-agents/**"), http())
                .before(uri("http://localhost:8086"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> nokiaNacMetadataRoutes() {
        return route("nokia-nac-metadata")
                .route(org.springframework.web.servlet.function.RequestPredicates.path("/nokia-nac/**"), http())
                .before(uri("http://localhost:8081"))
                .build();
    }
}
