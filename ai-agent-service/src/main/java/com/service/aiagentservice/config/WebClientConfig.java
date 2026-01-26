package com.service.aiagentservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * Configuration for WebClient beans used for external API calls
 */
@Configuration
public class WebClientConfig {
    
    @Value("${nokia.nac.base-url}")
    private String nokiaBaseUrl;
    
    @Value("${nokia.nac.rapidapi-key}")
    private String rapidApiKey;
    
    @Value("${nokia.nac.rapidapi-host}")
    private String rapidApiHost;
    
    @Value("${nokia.nac.timeout:30000}")
    private int nokiaTimeout;
    
    /**
     * WebClient for Nokia Network as Code API calls (RapidAPI)
     */
    @Bean(name = "nokiaWebClient")
    public WebClient nokiaWebClient() {
        return WebClient.builder()
                .baseUrl(nokiaBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-rapidapi-key", rapidApiKey)
                .defaultHeader("x-rapidapi-host", rapidApiHost)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }
    
    /**
     * WebClient for internal service-to-service communication
     */
    @Bean(name = "internalWebClient")
    public WebClient internalWebClient() {
        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }
}
