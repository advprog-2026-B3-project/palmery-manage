package id.ac.ui.cs.advprog.palmerymanage.pengiriman;

import id.ac.ui.cs.advprog.palmerymanage.event.PengirimanApprovedAdminEvent;
import id.ac.ui.cs.advprog.palmerymanage.event.PengirimanApprovedMandorEvent;
import id.ac.ui.cs.advprog.palmerymanage.event.PengirimanTibaEvent;
import id.ac.ui.cs.advprog.palmerymanage.model.Pengiriman;
import id.ac.ui.cs.advprog.palmerymanage.service.DomainEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class SpringPengirimanEventPublisher implements PengirimanEventPublisher {

    private final DomainEventPublisher domainEventPublisher;

    public SpringPengirimanEventPublisher(DomainEventPublisher domainEventPublisher) {
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
        domainEventPublisher.publish(
                "PengirimanApprovedMandor",
                buildShipmentPayload(event, pengiriman.getTotalKg(), "Pengiriman disetujui mandor")
        );
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

        Map<String, Object> payload = buildShipmentPayload(event, recognizedKg, "Pengiriman disetujui admin");
        payload.put("recognizedKg", recognizedKg);
        payload.put("kgDiakui", recognizedKg);
        payload.put("approvedKg", recognizedKg);

        domainEventPublisher.publish("PengirimanApprovedAdmin", payload);
    }

    private Map<String, Object> buildShipmentPayload(
            PengirimanTibaEvent event,
            int quantityKg,
            String title
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("pengirimanId", event.pengirimanId().toString());
        payload.put("userId", event.supirId());
        payload.put("supirUserId", event.supirId());
        payload.put("driverUserId", event.supirId());
        payload.put("mandorUserId", event.mandorId());
        payload.put("quantityKg", quantityKg);
        payload.put("kg", quantityKg);
        payload.put("totalKg", event.totalKg());
        payload.put("panenIds", event.panenIds());
        payload.put("targetUserIds", java.util.List.of(event.supirId(), event.mandorId()));
        payload.put("title", title);
        payload.put("occurredAt", event.timestamp().toString());
        return payload;
    }

    private Map<String, Object> buildShipmentPayload(
            PengirimanApprovedMandorEvent event,
            int quantityKg,
            String title
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("pengirimanId", event.pengirimanId().toString());
        payload.put("userId", event.supirId());
        payload.put("supirUserId", event.supirId());
        payload.put("driverUserId", event.supirId());
        payload.put("mandorUserId", event.mandorId());
        payload.put("quantityKg", quantityKg);
        payload.put("kg", quantityKg);
        payload.put("totalKg", event.totalKg());
        payload.put("panenIds", event.panenIds());
        payload.put("title", title);
        payload.put("message", "Payroll supir otomatis dibuat setelah pengiriman disetujui mandor.");
        payload.put("occurredAt", event.timestamp().toString());
        return payload;
    }

    private Map<String, Object> buildShipmentPayload(
            PengirimanApprovedAdminEvent event,
            int quantityKg,
            String title
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("pengirimanId", event.pengirimanId().toString());
        payload.put("userId", event.mandorId());
        payload.put("supirUserId", event.supirId());
        payload.put("mandorUserId", event.mandorId());
        payload.put("quantityKg", quantityKg);
        payload.put("kg", quantityKg);
        payload.put("totalKg", event.totalKg());
        payload.put("panenIds", event.panenIds());
        payload.put("title", title);
        payload.put("message", "Payroll mandor otomatis dibuat dari pengiriman yang diakui admin.");
        payload.put("occurredAt", event.timestamp().toString());
        return payload;
    }
}
