package id.ac.ui.cs.advprog.palmerymanage.pengiriman;

import id.ac.ui.cs.advprog.palmerymanage.event.PengirimanApprovedAdminEvent;
import id.ac.ui.cs.advprog.palmerymanage.event.PengirimanApprovedMandorEvent;
import id.ac.ui.cs.advprog.palmerymanage.event.PengirimanTibaEvent;
import id.ac.ui.cs.advprog.palmerymanage.model.AdminApprovalStatus;
import id.ac.ui.cs.advprog.palmerymanage.model.Pengiriman;
import id.ac.ui.cs.advprog.palmerymanage.service.DomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class SpringPengirimanEventPublisher implements PengirimanEventPublisher {

    private final ApplicationEventPublisher publisher;
    private final DomainEventPublisher domainEventPublisher;

    public SpringPengirimanEventPublisher(ApplicationEventPublisher publisher, DomainEventPublisher domainEventPublisher) {
        this.publisher = publisher;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public void publishPengirimanTiba(Pengiriman pengiriman) {
        PengirimanTibaEvent event = new PengirimanTibaEvent(
                pengiriman.getId(),
                pengiriman.getSupirId(),
                pengiriman.getMandorId(),
                pengiriman.getTotalKg(),
                pengiriman.getPanenIds(),
                Instant.now()
        );
        publisher.publishEvent(event);
        domainEventPublisher.publish("PengirimanTiba", buildShipmentPayload(event, pengiriman.getTotalKg(), "Pengiriman tiba"));
    }

    @Override
    public void publishPengirimanApprovedMandor(Pengiriman pengiriman) {
        PengirimanApprovedMandorEvent event = new PengirimanApprovedMandorEvent(
                pengiriman.getId(),
                pengiriman.getSupirId(),
                pengiriman.getMandorId(),
                pengiriman.getTotalKg(),
                pengiriman.getPanenIds(),
                Instant.now()
        );
        publisher.publishEvent(event);
        domainEventPublisher.publish("PENGIRIMAN_APPROVED_BY_MANDOR", pengirimanApprovedMandorPayload(pengiriman));
    }

    @Override
    public void publishPengirimanApprovedAdmin(Pengiriman pengiriman, int recognizedKg) {
        PengirimanApprovedAdminEvent event = new PengirimanApprovedAdminEvent(
                pengiriman.getId(),
                pengiriman.getSupirId(),
                pengiriman.getMandorId(),
                pengiriman.getTotalKg(),
                recognizedKg,
                pengiriman.getPanenIds(),
                Instant.now()
        );
        publisher.publishEvent(event);

        String eventType = pengiriman.getAdminApprovalStatus() == AdminApprovalStatus.PARTIALLY_APPROVED
                ? "PENGIRIMAN_PARTIALLY_APPROVED_BY_ADMIN"
                : "PENGIRIMAN_APPROVED_BY_ADMIN";
        domainEventPublisher.publish(eventType, pengirimanApprovedAdminPayload(pengiriman, recognizedKg));
    }

    private Map<String, Object> pengirimanApprovedMandorPayload(Pengiriman pengiriman) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("pengirimanId", pengiriman.getId().toString());
        payload.put("supirId", pengiriman.getSupirId());
        payload.put("mandorId", pengiriman.getMandorId());
        payload.put("kebunId", pengiriman.getKebunId());
        payload.put("userId", pengiriman.getSupirId());
        payload.put("quantityKg", BigDecimal.valueOf(pengiriman.getTotalKg()));
        payload.put("totalKg", pengiriman.getTotalKg());
        payload.put("panenIds", pengiriman.getPanenIds());
        payload.put("description", "Pengiriman disetujui mandor untuk " + pengiriman.getTotalKg() + " Kg");
        payload.put("title", "Pengiriman disetujui mandor");
        return payload;
    }

    private Map<String, Object> pengirimanApprovedAdminPayload(Pengiriman pengiriman, int recognizedKg) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("pengirimanId", pengiriman.getId().toString());
        payload.put("supirId", pengiriman.getSupirId());
        payload.put("mandorId", pengiriman.getMandorId());
        payload.put("kebunId", pengiriman.getKebunId());
        payload.put("userId", pengiriman.getMandorId());
        payload.put("quantityKg", BigDecimal.valueOf(recognizedKg));
        payload.put("recognizedKg", recognizedKg);
        payload.put("acceptedKgByAdmin", recognizedKg);
        payload.put("totalKg", pengiriman.getTotalKg());
        payload.put("panenIds", pengiriman.getPanenIds());
        payload.put("description", "Pengiriman diakui admin untuk " + recognizedKg + " Kg");
        payload.put("title", "Pengiriman disetujui admin");
        return payload;
    }

    private Map<String, Object> buildShipmentPayload(PengirimanTibaEvent event, int totalKg, String title) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("pengirimanId", event.pengirimanId().toString());
        payload.put("supirId", event.supirId());
        payload.put("mandorId", event.mandorId());
        payload.put("userId", event.supirId());
        payload.put("quantityKg", BigDecimal.valueOf(totalKg));
        payload.put("totalKg", totalKg);
        payload.put("panenIds", event.panenIds());
        payload.put("description", title + " dengan total " + totalKg + " Kg");
        payload.put("title", title);
        payload.put("timestamp", event.timestamp());
        return payload;
    }
}
