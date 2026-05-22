package id.ac.ui.cs.advprog.palmerymanage.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HarvestEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.harvest:harvest_exchange}")
    private String exchange;

    @Value("${rabbitmq.routingkey.harvest.approved:harvest_approved_routing_key}")
    private String routingKeyApproved;

    @Value("${rabbitmq.routingkey.harvest.submitted:harvest_submitted_routing_key}")
    private String routingKeySubmitted;

    public HarvestEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishHarvestApproved(@NonNull HarvestApprovedEvent event) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKeyApproved, event);
            log.info("[RabbitMQ] HarvestApproved published: harvestId={}", event.harvestId());
        } catch (Exception e) {
            log.warn("[RabbitMQ] Failed to publish HarvestApproved (broker likely unavailable): harvestId={}, error={}",
                    event.harvestId(), e.getMessage());
        }
    }

    public void publishHarvestSubmitted(@NonNull HarvestSubmittedEvent event) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKeySubmitted, event);
            log.info("[RabbitMQ] HarvestSubmitted published: harvestId={}", event.harvestId());
        } catch (Exception e) {
            log.warn("[RabbitMQ] Failed to publish HarvestSubmitted (broker likely unavailable): harvestId={}, error={}",
                    event.harvestId(), e.getMessage());
        }
    }
}
