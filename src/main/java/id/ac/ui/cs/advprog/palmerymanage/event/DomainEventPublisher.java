package id.ac.ui.cs.advprog.palmerymanage.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class DomainEventPublisher {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final String queueName;

    public DomainEventPublisher(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            @Value("${app.event.queue:palmery.domain.events}") String queueName
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.queueName = queueName;
    }

    public DomainEventMessage publish(String eventType, Map<String, Object> payload) {
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("eventType is required");
        }

        DomainEventMessage event = new DomainEventMessage(
                UUID.randomUUID().toString(),
                eventType.trim(),
                Instant.now(),
                0,
                payload == null ? Map.of() : payload
        );

        try {
            redisTemplate.opsForList().rightPush(queueName, objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize domain event", exception);
        }

        return event;
    }
}