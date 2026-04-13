package id.ac.ui.cs.advprog.palmerymanage.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HarvestEventPublisherTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private HarvestEventPublisher harvestEventPublisher;

    @Test
    void testPublishHarvestApproved() {
        UUID harvestId = UUID.randomUUID();
        HarvestApprovedEvent event = new HarvestApprovedEvent(
                harvestId, "worker-1", "mandor-1", "plantation-1", 50.0f, Instant.now()
        );

        harvestEventPublisher.publishHarvestApproved(event);

        verify(applicationEventPublisher, times(1)).publishEvent(event);
    }
}
