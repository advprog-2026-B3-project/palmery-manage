package id.ac.ui.cs.advprog.palmerymanage.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class HarvestEventPublisherTest {

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private HarvestEventPublisher harvestEventPublisher;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(harvestEventPublisher, "exchange", "harvest_exchange");
        ReflectionTestUtils.setField(harvestEventPublisher, "routingKeyApproved", "harvest_approved_routing_key");
        ReflectionTestUtils.setField(harvestEventPublisher, "routingKeySubmitted", "harvest_submitted_routing_key");
    }

    @Test
    void testPublishHarvestApproved() {
        UUID harvestId = UUID.randomUUID();
        HarvestApprovedEvent event = new HarvestApprovedEvent(
                harvestId, "worker-1", "mandor-1", "plantation-1", 50.0f, Instant.now()
        );

        harvestEventPublisher.publishHarvestApproved(event);

        verify(rabbitTemplate, times(1)).convertAndSend("harvest_exchange", "harvest_approved_routing_key", event);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> payloadCaptor =
                ArgumentCaptor.forClass((Class<Map<String, Object>>) (Class<?>) Map.class);
        verify(domainEventPublisher).publish(eq("HASIL_PANEN_APPROVED"), payloadCaptor.capture());

        Map<String, Object> payload = payloadCaptor.getValue();
        assertEquals(harvestId.toString(), payload.get("harvestId"));
        assertEquals("worker-1", payload.get("workerId"));
        assertEquals("worker-1", payload.get("userId"));
        assertEquals("worker-1", payload.get("buruhUserId"));
        assertEquals("mandor-1", payload.get("mandorId"));
        assertEquals("plantation-1", payload.get("plantationId"));
        assertEquals(BigDecimal.valueOf(50.0f), payload.get("quantityKg"));
        assertEquals(BigDecimal.valueOf(50.0f), payload.get("kgHarvested"));
        assertEquals("Hasil panen disetujui untuk 50.0 Kg", payload.get("description"));
        assertEquals("Panen disetujui", payload.get("title"));
        assertEquals(event.timestamp(), payload.get("timestamp"));
    }

    @Test
    void testPublishHarvestSubmitted() {
        UUID harvestId = UUID.randomUUID();
        HarvestSubmittedEvent event = new HarvestSubmittedEvent(
                harvestId, "worker-1", "plantation-1", 50.0f, Instant.now()
        );

        harvestEventPublisher.publishHarvestSubmitted(event);

        verifyNoInteractions(rabbitTemplate);
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(domainEventPublisher, times(1)).publish(eq("PanenSubmitted"), payloadCaptor.capture());

        Map<String, Object> payload = payloadCaptor.getValue();
        assertEquals(harvestId.toString(), payload.get("harvestId"));
        assertEquals("worker-1", payload.get("userId"));
        assertEquals(50.0f, payload.get("kgHarvested"));
    }
}
