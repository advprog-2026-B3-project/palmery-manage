package id.ac.ui.cs.advprog.palmerymanage.pengiriman;

import id.ac.ui.cs.advprog.palmerymanage.event.PengirimanApprovedAdminEvent;
import id.ac.ui.cs.advprog.palmerymanage.event.PengirimanApprovedMandorEvent;
import id.ac.ui.cs.advprog.palmerymanage.event.PengirimanTibaEvent;
import id.ac.ui.cs.advprog.palmerymanage.model.Pengiriman;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SpringPengirimanEventPublisher implements PengirimanEventPublisher {

    private final ApplicationEventPublisher publisher;

    public SpringPengirimanEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publishPengirimanTiba(Pengiriman pengiriman) {
        publisher.publishEvent(new PengirimanTibaEvent(
                pengiriman.getId(),
                pengiriman.getSupirId(),
                pengiriman.getMandorId(),
                pengiriman.getTotalKg(),
                pengiriman.getPanenIds(),
                Instant.now()
        ));
    }

    @Override
    public void publishPengirimanApprovedMandor(Pengiriman pengiriman) {
        publisher.publishEvent(new PengirimanApprovedMandorEvent(
                pengiriman.getId(),
                pengiriman.getSupirId(),
                pengiriman.getMandorId(),
                pengiriman.getTotalKg(),
                pengiriman.getPanenIds(),
                Instant.now()
        ));
    }

    @Override
    public void publishPengirimanApprovedAdmin(Pengiriman pengiriman, int recognizedKg) {
        publisher.publishEvent(new PengirimanApprovedAdminEvent(
                pengiriman.getId(),
                pengiriman.getSupirId(),
                pengiriman.getMandorId(),
                pengiriman.getTotalKg(),
                recognizedKg,
                pengiriman.getPanenIds(),
                Instant.now()
        ));
    }
}
