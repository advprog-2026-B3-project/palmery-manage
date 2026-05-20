package id.ac.ui.cs.advprog.palmerymanage.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HarvestEventPublisherTest {

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
    }

    @Test
    void testPublishHarvestSubmitted() {
        UUID harvestId = UUID.randomUUID();
        HarvestSubmittedEvent event = new HarvestSubmittedEvent(
                harvestId, "worker-1", "plantation-1", 50.0f, Instant.now()
        );

        harvestEventPublisher.publishHarvestSubmitted(event);

        verify(rabbitTemplate, times(1)).convertAndSend("harvest_exchange", "harvest_submitted_routing_key", event);
    }
}
