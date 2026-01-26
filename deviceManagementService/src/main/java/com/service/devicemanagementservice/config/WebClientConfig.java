package com.service.devicemanagementservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${nokia.nac.base-url:https://network-as-code.p-eu.rapidapi.com}")
    private String nokiaBaseUrl;

    @Value("${nokia.nac.rapidapi-key:}")
    private String rapidApiKey;

    @Value("${nokia.nac.rapidapi-host:network-as-code.nokia.rapidapi.com}")
    private String rapidApiHost;

    @Value("${nokia.nac.timeout:30000}")
    private int timeout;

    /**
     * WebClient for service-to-service internal calls
     * Used for communication between microservices
     */
    @Bean(name = "internalWebClient")
    public WebClient internalWebClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(timeout))
                .followRedirect(true);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    /**
     * WebClient for Nokia Network as Code API calls via RapidAPI
     * Configured with RapidAPI specific headers
     */
    @Bean(name = "nokiaWebClient")
    public WebClient nokiaWebClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(timeout))
                .followRedirect(true);

        return WebClient.builder()
                .baseUrl(nokiaBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .defaultHeader("x-rapidapi-key", rapidApiKey)
                .defaultHeader("x-rapidapi-host", rapidApiHost)
                .build();
    }
}
