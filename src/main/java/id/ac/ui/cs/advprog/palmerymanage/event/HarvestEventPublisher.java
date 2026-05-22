package id.ac.ui.cs.advprog.palmerymanage.event;

import id.ac.ui.cs.advprog.palmerymanage.service.DomainEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class HarvestEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final DomainEventPublisher domainEventPublisher;

    @Value("${rabbitmq.exchange.harvest:harvest_exchange}")
    private String exchange = "harvest_exchange";

    @Value("${rabbitmq.routingkey.harvest.approved:harvest_approved_routing_key}")
    private String routingKeyApproved = "harvest_approved_routing_key";

    @Value("${rabbitmq.routingkey.harvest.submitted:harvest_submitted_routing_key}")
    private String routingKeySubmitted = "harvest_submitted_routing_key";

    public HarvestEventPublisher(RabbitTemplate rabbitTemplate, DomainEventPublisher domainEventPublisher) {
        this.rabbitTemplate = rabbitTemplate;
        this.domainEventPublisher = domainEventPublisher;
    }

    public void publishHarvestApproved(@NonNull HarvestApprovedEvent event) {
        rabbitTemplate.convertAndSend(exchange, routingKeyApproved, event);
        domainEventPublisher.publish("HASIL_PANEN_APPROVED", harvestApprovedPayload(event));
        log.info("[RabbitMQ] HarvestApproved published: harvestId={}", event.harvestId());
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

    private Map<String, Object> harvestApprovedPayload(HarvestApprovedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("harvestId", event.harvestId().toString());
        payload.put("workerId", event.workerId());
        payload.put("userId", event.workerId());
        payload.put("buruhUserId", event.workerId());
        payload.put("mandorId", event.mandorId());
        payload.put("plantationId", event.plantationId());
        payload.put("quantityKg", BigDecimal.valueOf(event.kgHarvested()));
        payload.put("kgHarvested", BigDecimal.valueOf(event.kgHarvested()));
        payload.put("description", "Hasil panen disetujui untuk " + event.kgHarvested() + " Kg");
        payload.put("title", "Panen disetujui");
        payload.put("timestamp", event.timestamp());
        return payload;
    }
}
