package id.ac.ui.cs.advprog.palmerymanage.pengiriman;

import id.ac.ui.cs.advprog.palmerymanage.event.DomainEventPublisher;
import id.ac.ui.cs.advprog.palmerymanage.model.Pengiriman;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class SpringPengirimanEventPublisher implements PengirimanEventPublisher {

    private final DomainEventPublisher domainEventPublisher;

    public SpringPengirimanEventPublisher(DomainEventPublisher domainEventPublisher) {
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public void publishPengirimanTiba(Pengiriman pengiriman) {
        domainEventPublisher.publish("PengirimanTiba", shipmentArrivedPayload(pengiriman));
    }

    @Override
    public void publishPengirimanApprovedMandor(Pengiriman pengiriman) {
        domainEventPublisher.publish("PENGIRIMAN_APPROVED_BY_MANDOR", pengirimanApprovedMandorPayload(pengiriman));
    }

    @Override
    public void publishPengirimanApprovedAdmin(Pengiriman pengiriman, int recognizedKg) {
        domainEventPublisher.publish("PENGIRIMAN_APPROVED_BY_ADMIN", pengirimanApprovedAdminPayload(pengiriman, recognizedKg));
    }

    private Map<String, Object> shipmentArrivedPayload(Pengiriman pengiriman) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("pengirimanId", pengiriman.getId().toString());
        payload.put("supirId", pengiriman.getSupirId());
        payload.put("mandorId", pengiriman.getMandorId());
        payload.put("userId", pengiriman.getSupirId());
        payload.put("totalKg", pengiriman.getTotalKg());
        payload.put("quantityKg", BigDecimal.valueOf(pengiriman.getTotalKg()));
        payload.put("panenIds", pengiriman.getPanenIds());
        payload.put("description", "Pengiriman tiba untuk " + pengiriman.getTotalKg() + " Kg");
        payload.put("title", "Pengiriman tiba");
        return payload;
    }

    private Map<String, Object> pengirimanApprovedMandorPayload(Pengiriman pengiriman) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("pengirimanId", pengiriman.getId().toString());
        payload.put("supirId", pengiriman.getSupirId());
        payload.put("mandorId", pengiriman.getMandorId());
        payload.put("userId", pengiriman.getSupirId());
        payload.put("quantityKg", BigDecimal.valueOf(pengiriman.getTotalKg()));
        payload.put("totalKg", pengiriman.getTotalKg());
        payload.put("panenIds", pengiriman.getPanenIds());
        payload.put("description", "Pengiriman disetujui mandor untuk " + pengiriman.getTotalKg() + " Kg");
        payload.put("title", "Pengiriman disetujui mandor");
        return payload;
    }

    private Map<String, Object> pengirimanApprovedAdminPayload(Pengiriman pengiriman, int recognizedKg) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("pengirimanId", pengiriman.getId().toString());
        payload.put("supirId", pengiriman.getSupirId());
        payload.put("mandorId", pengiriman.getMandorId());
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
}
