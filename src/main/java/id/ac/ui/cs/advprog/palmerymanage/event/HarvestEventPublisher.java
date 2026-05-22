package id.ac.ui.cs.advprog.palmerymanage.event;

import id.ac.ui.cs.advprog.palmerymanage.service.DomainEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class HarvestEventPublisher {

    private final DomainEventPublisher domainEventPublisher;

    public HarvestEventPublisher(DomainEventPublisher domainEventPublisher) {
        this.domainEventPublisher = domainEventPublisher;
    }

    public void publishHarvestApproved(@NonNull HarvestApprovedEvent event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("harvestId", event.harvestId().toString());
        payload.put("userId", event.workerId());
        payload.put("buruhUserId", event.workerId());
        payload.put("workerUserId", event.workerId());
        payload.put("mandorUserId", event.mandorId());
        payload.put("plantationId", event.plantationId());
        payload.put("quantityKg", event.kgHarvested());
        payload.put("kg", event.kgHarvested());
        payload.put("kgHarvested", event.kgHarvested());
        payload.put("title", "Panen disetujui");
        payload.put("message", "Payroll buruh otomatis dibuat dari panen yang disetujui.");
        payload.put("occurredAt", event.timestamp().toString());

        domainEventPublisher.publish("PanenApproved", payload);
        log.info("[EVENT] HarvestApproved published: harvestId={}, workerId={}, kg={}",
                event.harvestId(), event.workerId(), event.kgHarvested());
    }

    public void publishHarvestSubmitted(@NonNull HarvestSubmittedEvent event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("harvestId", event.harvestId().toString());
        payload.put("userId", event.workerId());
        payload.put("buruhUserId", event.workerId());
        payload.put("workerUserId", event.workerId());
        payload.put("plantationId", event.plantationId());
        payload.put("quantityKg", event.kgHarvested());
        payload.put("kg", event.kgHarvested());
        payload.put("kgHarvested", event.kgHarvested());
        payload.put("title", "Panen dikirim");
        payload.put("message", "Panen baru dikirim dan menunggu review mandor.");
        payload.put("occurredAt", event.timestamp().toString());

        domainEventPublisher.publish("PanenSubmitted", payload);
        log.info("[EVENT] HarvestSubmitted published: harvestId={}, workerId={}, kg={}",
                event.harvestId(), event.workerId(), event.kgHarvested());
    }
}
