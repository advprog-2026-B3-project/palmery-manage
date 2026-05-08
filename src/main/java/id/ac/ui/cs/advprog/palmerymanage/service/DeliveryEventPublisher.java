package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.event.PengirimanApprovedAdminEvent;
import id.ac.ui.cs.advprog.palmerymanage.event.PengirimanApprovedMandorEvent;
import id.ac.ui.cs.advprog.palmerymanage.event.PengirimanTibaEvent;
import id.ac.ui.cs.advprog.palmerymanage.model.Delivery;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DeliveryEventPublisher {

    private final ApplicationEventPublisher publisher;

    public DeliveryEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishPengirimanTiba(Delivery delivery) {
        publisher.publishEvent(new PengirimanTibaEvent(
                delivery.getId(),
                delivery.getSupirId(),
                delivery.getMandorId(),
                delivery.getTotalKg(),
                delivery.getPanenIds(),
                Instant.now()
        ));
    }

    public void publishPengirimanApprovedMandor(Delivery delivery) {
        publisher.publishEvent(new PengirimanApprovedMandorEvent(
                delivery.getId(),
                delivery.getSupirId(),
                delivery.getMandorId(),
                delivery.getTotalKg(),
                delivery.getPanenIds(),
                Instant.now()
        ));
    }

    public void publishPengirimanApprovedAdmin(Delivery delivery, int recognizedKg) {
        publisher.publishEvent(new PengirimanApprovedAdminEvent(
                delivery.getId(),
                delivery.getSupirId(),
                delivery.getMandorId(),
                delivery.getTotalKg(),
                recognizedKg,
                delivery.getPanenIds(),
                Instant.now()
        ));
    }
}

