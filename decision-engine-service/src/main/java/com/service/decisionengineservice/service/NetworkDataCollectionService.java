package com.service.decisionengineservice.service;

import com.service.decisionengineservice.agent.model.NetworkData;
import com.service.shared.service.InternalServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for collecting real-time network data from Nokia APIs and internal services
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NetworkDataCollectionService {
    
    @Qualifier("nokiaWebClient")
    private final WebClient nokiaWebClient;
    private final InternalServiceClient internalServiceClient;
    
    @Value("${services.connectivity.base-url:http://localhost:8081}")
    private String connectivityServiceUrl;
    
    @Value("${services.identification.base-url:http://localhost:8082}")
    private String identificationServiceUrl;
    
    @Value("${services.location.base-url:http://localhost:8083}")
    private String locationServiceUrl;
    
    @Value("${ai.agents.data-collection.interval:10}")
    private int collectionInterval;
    
    // Cache for collected data
    private final Map<String, NetworkData> dataCache = new ConcurrentHashMap<>();
    
    /**
     * Collect network data for a specific device/phone number
     */
    public Mono<NetworkData> collectNetworkData(String phoneNumber) {
        return Mono.zip(
                collectLocationData(phoneNumber),
                collectDeviceStatus(phoneNumber),
                collectConnectivityStatus(phoneNumber),
                collectQoSData(phoneNumber)
        ).map(tuple -> {
            NetworkData data = new NetworkData();
            data.setLocation(tuple.getT1());
            data.setDeviceStatus(tuple.getT2());
            data.setConnectivity(tuple.getT3());
            data.setQos(tuple.getT4());
            
            // Cache the data
            dataCache.put(phoneNumber, data);
            
            return data;
        }).doOnError(error -> log.error("Error collecting network data for {}", phoneNumber, error))
        .onErrorReturn(new NetworkData());
    }
    
    /**
     * Collect location data from Nokia Location API
     */
    private Mono<NetworkData.LocationData> collectLocationData(String phoneNumber) {
        Map<String, Object> requestBody = Map.of(
            "device", Map.of("phoneNumber", phoneNumber),
            "maxAge", 60
        );
        
        return nokiaWebClient.post()
                .uri("/location-retrieval/v0/retrieve")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    NetworkData.LocationData location = new NetworkData.LocationData();
                    if (response.containsKey("location")) {
                        Map<String, Object> loc = (Map<String, Object>) response.get("location");
                        if (loc.containsKey("latitude")) {
                            location.setLatitude(Double.parseDouble(loc.get("latitude").toString()));
                        }
                        if (loc.containsKey("longitude")) {
                            location.setLongitude(Double.parseDouble(loc.get("longitude").toString()));
                        }
                        if (loc.containsKey("accuracy")) {
                            location.setAccuracy(Double.parseDouble(loc.get("accuracy").toString()));
                        }
                    }
                    location.setMaxAge(60);
                    return location;
                })
                .onErrorReturn(new NetworkData.LocationData());
    }
    
    /**
     * Collect device status from Identification Service
     */
    private Mono<NetworkData.DeviceStatus> collectDeviceStatus(String phoneNumber) {
        return internalServiceClient.getFromService(identificationServiceUrl, 
                "/identification/share-phone-number?phoneNumber=" + phoneNumber)
                .map(response -> {
                    NetworkData.DeviceStatus status = new NetworkData.DeviceStatus();
                    if (response != null && response.containsKey("data")) {
                        Map<String, Object> data = (Map<String, Object>) response.get("data");
                        status.setStatus((String) data.getOrDefault("status", "UNKNOWN"));
                        status.setDeviceId((String) data.getOrDefault("deviceId", ""));
                        status.setImei((String) data.getOrDefault("imei", ""));
                        status.setIsActive(Boolean.TRUE.equals(data.getOrDefault("isActive", false)));
                    }
                    return status;
                })
                .onErrorReturn(new NetworkData.DeviceStatus());
    }
    
    /**
     * Collect connectivity status from Connectivity Service
     */
    private Mono<NetworkData.ConnectivityMetrics> collectConnectivityStatus(String phoneNumber) {
        Map<String, Object> requestBody = Map.of(
            "device", Map.of("phoneNumber", phoneNumber)
        );
        
        return internalServiceClient.callService(connectivityServiceUrl, 
                "/connectivity/Qos/sessions", requestBody)
                .map(response -> {
                    NetworkData.ConnectivityMetrics metrics = new NetworkData.ConnectivityMetrics();
                    if (response != null && response.containsKey("data")) {
                        Map<String, Object> data = (Map<String, Object>) response.get("data");
                        metrics.setStatus((String) data.getOrDefault("status", "UNKNOWN"));
                        metrics.setSignalStrength(((Number) data.getOrDefault("signalStrength", 0)).intValue());
                        metrics.setNetworkType((String) data.getOrDefault("networkType", "UNKNOWN"));
                        metrics.setLatency(((Number) data.getOrDefault("latency", 0.0)).doubleValue());
                        metrics.setIsConnected(Boolean.TRUE.equals(data.getOrDefault("isConnected", false)));
                    }
                    return metrics;
                })
                .onErrorReturn(new NetworkData.ConnectivityMetrics());
    }
    
    /**
     * Collect QoS data from Connectivity Service
     */
    private Mono<NetworkData.QoSMetrics> collectQoSData(String phoneNumber) {
        Map<String, Object> requestBody = Map.of(
            "device", Map.of("phoneNumber", phoneNumber)
        );
        
        return internalServiceClient.callService(connectivityServiceUrl, 
                "/connectivity/Qos/sessions", requestBody)
                .map(response -> {
                    NetworkData.QoSMetrics qos = new NetworkData.QoSMetrics();
                    if (response != null && response.containsKey("data")) {
                        Map<String, Object> data = (Map<String, Object>) response.get("data");
                        if (data.containsKey("sessions") && ((java.util.List<?>) data.get("sessions")).size() > 0) {
                            Map<String, Object> session = (Map<String, Object>) ((java.util.List<?>) data.get("sessions")).get(0);
                            qos.setQosProfile((String) session.getOrDefault("qosProfile", "DEFAULT"));
                            qos.setPriority(((Number) session.getOrDefault("priority", 5)).intValue());
                            qos.setBandwidth(((Number) session.getOrDefault("bandwidth", 0.0)).doubleValue());
                            qos.setLatency(((Number) session.getOrDefault("latency", 0.0)).doubleValue());
                        }
                    }
                    return qos;
                })
                .onErrorReturn(new NetworkData.QoSMetrics());
    }
    
    /**
     * Get cached network data
     */
    public NetworkData getCachedData(String phoneNumber) {
        return dataCache.getOrDefault(phoneNumber, new NetworkData());
    }
    
    /**
     * Clear cache for a specific phone number
     */
    public void clearCache(String phoneNumber) {
        dataCache.remove(phoneNumber);
    }
    
    /**
     * Clear all cached data
     */
    public void clearAllCache() {
        dataCache.clear();
    }
}
