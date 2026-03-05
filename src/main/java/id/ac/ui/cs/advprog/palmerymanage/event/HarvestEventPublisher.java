package id.ac.ui.cs.advprog.palmerymanage.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class HarvestEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public HarvestEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishHarvestApproved(HarvestApprovedEvent event) {
        applicationEventPublisher.publishEvent(event);
        System.out.println("[EVENT] HarvestApproved dikirim: harvestId=" + event.harvestId()
                + ", workerId=" + event.workerId()
                + ", kg=" + event.kgHarvested());
    }
}