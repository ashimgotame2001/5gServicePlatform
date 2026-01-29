package com.service.shared.event;

import com.service.shared.dto.EmergencyEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka Producer for Emergency Events
 * Publishes emergency events for parallel consumption by multiple services
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmergencyEventProducer {

    private static final String EMERGENCY_EVENTS_TOPIC = "emergency-events";

    private final KafkaTemplate<String, EmergencyEventDTO> emergencyEventKafkaTemplate;

    /**
     * Publish emergency event to Kafka
     * 
     * @param event Emergency event to publish
     * @return CompletableFuture with send result
     */
    public CompletableFuture<SendResult<String, EmergencyEventDTO>> publishEmergencyEvent(EmergencyEventDTO event) {
        log.info("Publishing emergency event: {} for emergency: {}", 
                event.getEventType(), event.getEmergencyContext().getEmergencyId());
        
        String key = event.getEmergencyContext() != null ? 
                event.getEmergencyContext().getEmergencyId() : event.getEventId();
        return emergencyEventKafkaTemplate.send(EMERGENCY_EVENTS_TOPIC, key, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Emergency event published successfully: {}", event.getEventId());
                    } else {
                        log.error("Failed to publish emergency event: {}", event.getEventId(), ex);
                    }
                });
    }

    /**
     * Publish emergency detected event
     */
    public CompletableFuture<SendResult<String, EmergencyEventDTO>> publishEmergencyDetected(
            EmergencyEventDTO event) {
        event.setEventType(EmergencyEventDTO.EventType.EMERGENCY_DETECTED);
        return publishEmergencyEvent(event);
    }

    /**
     * Publish emergency resolved event
     */
    public CompletableFuture<SendResult<String, EmergencyEventDTO>> publishEmergencyResolved(
            EmergencyEventDTO event) {
        event.setEventType(EmergencyEventDTO.EventType.EMERGENCY_RESOLVED);
        return publishEmergencyEvent(event);
    }
}
