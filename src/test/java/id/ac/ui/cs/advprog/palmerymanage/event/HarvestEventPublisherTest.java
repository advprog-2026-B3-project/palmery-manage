package id.ac.ui.cs.advprog.palmerymanage.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HarvestEventPublisherTest {

    @Mock
    private id.ac.ui.cs.advprog.palmerymanage.service.DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private HarvestEventPublisher harvestEventPublisher;

    @BeforeEach
    void setUp() {}

    @Test
    void testPublishHarvestApproved() {
        UUID harvestId = UUID.randomUUID();
        HarvestApprovedEvent event = new HarvestApprovedEvent(
                harvestId, "worker-1", "mandor-1", "plantation-1", 50.0f, Instant.now()
        );

        harvestEventPublisher.publishHarvestApproved(event);

        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(domainEventPublisher, times(1)).publish(eq("PanenApproved"), payloadCaptor.capture());
        Map<String, Object> payload = payloadCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(harvestId.toString(), payload.get("harvestId"));
        org.junit.jupiter.api.Assertions.assertEquals("worker-1", payload.get("userId"));
        org.junit.jupiter.api.Assertions.assertEquals(50.0f, payload.get("kgHarvested"));
    }

    @Test
    void testPublishHarvestSubmitted() {
        UUID harvestId = UUID.randomUUID();
        HarvestSubmittedEvent event = new HarvestSubmittedEvent(
                harvestId, "worker-1", "plantation-1", 50.0f, Instant.now()
        );

        harvestEventPublisher.publishHarvestSubmitted(event);

        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(domainEventPublisher, times(1)).publish(eq("PanenSubmitted"), payloadCaptor.capture());
        Map<String, Object> payload = payloadCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(harvestId.toString(), payload.get("harvestId"));
        org.junit.jupiter.api.Assertions.assertEquals("worker-1", payload.get("userId"));
        org.junit.jupiter.api.Assertions.assertEquals(50.0f, payload.get("kgHarvested"));
    }
}
