package id.ac.ui.cs.advprog.palmerymanage.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HarvestEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public HarvestEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishHarvestApproved(@NonNull HarvestApprovedEvent event) {
        applicationEventPublisher.publishEvent(event);
        log.info("[EVENT] HarvestApproved published: harvestId={}, workerId={}, kg={}",
                event.harvestId(), event.workerId(), event.kgHarvested());
    }
}
