package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.event.PengirimanApprovedAdminEvent;
import id.ac.ui.cs.advprog.palmerymanage.event.PengirimanApprovedMandorEvent;
import id.ac.ui.cs.advprog.palmerymanage.event.PengirimanTibaEvent;
import id.ac.ui.cs.advprog.palmerymanage.model.Pengiriman;
import id.ac.ui.cs.advprog.palmerymanage.pengiriman.PengirimanEventPublisher;
import id.ac.ui.cs.advprog.palmerymanage.pengiriman.SpringPengirimanEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PengirimanEventPublisherTest {

    @Mock
    private DomainEventPublisher domainEventPublisher;

    private PengirimanEventPublisher pengirimanEventPublisher;

    private Pengiriman pengiriman;

    @BeforeEach
    void setUp() {
        pengirimanEventPublisher = new SpringPengirimanEventPublisher(domainEventPublisher);

        pengiriman = new Pengiriman();
        pengiriman.setId(UUID.randomUUID());
        pengiriman.setSupirId("DRV-1");
        pengiriman.setMandorId("MDR-1");
        pengiriman.setTotalKg(300);
        pengiriman.setPanenIds(List.of("PAN-1", "PAN-2"));
    }

    @Test
    void publishesPengirimanTibaEvent() {
        pengirimanEventPublisher.publishPengirimanTiba(pengiriman);

        verify(domainEventPublisher).publish(eq("PengirimanTiba"), anyMap());
    }

    @Test
    void publishesPengirimanApprovedMandorEvent() {
        pengirimanEventPublisher.publishPengirimanApprovedMandor(pengiriman);

        verify(domainEventPublisher).publish(eq("PengirimanApprovedMandor"), anyMap());
    }

    @Test
    void publishesPengirimanApprovedAdminEventWithRecognizedKg() {
        pengirimanEventPublisher.publishPengirimanApprovedAdmin(pengiriman, 250);

        verify(domainEventPublisher).publish(eq("PengirimanApprovedAdmin"), anyMap());
    }
}
