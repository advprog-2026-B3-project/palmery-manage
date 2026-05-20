package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.event.PengirimanApprovedAdminEvent;
import id.ac.ui.cs.advprog.palmerymanage.event.PengirimanApprovedMandorEvent;
import id.ac.ui.cs.advprog.palmerymanage.event.PengirimanTibaEvent;
import id.ac.ui.cs.advprog.palmerymanage.model.Pengiriman;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PengirimanEventPublisherTest {

    @Mock
    private ApplicationEventPublisher publisher;

    private PengirimanEventPublisher pengirimanEventPublisher;

    private Pengiriman pengiriman;

    @BeforeEach
    void setUp() {
        pengirimanEventPublisher = new PengirimanEventPublisher(publisher);

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

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(captor.capture());
        Object event = captor.getValue();
        assertTrue(event instanceof PengirimanTibaEvent);
        PengirimanTibaEvent tiba = (PengirimanTibaEvent) event;
        assertEquals(pengiriman.getId(), tiba.pengirimanId());
        assertEquals(pengiriman.getSupirId(), tiba.supirId());
        assertEquals(pengiriman.getMandorId(), tiba.mandorId());
        assertEquals(pengiriman.getTotalKg(), tiba.totalKg());
        assertEquals(pengiriman.getPanenIds(), tiba.panenIds());
    }

    @Test
    void publishesPengirimanApprovedMandorEvent() {
        pengirimanEventPublisher.publishPengirimanApprovedMandor(pengiriman);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(captor.capture());
        Object event = captor.getValue();
        assertTrue(event instanceof PengirimanApprovedMandorEvent);
        PengirimanApprovedMandorEvent approved = (PengirimanApprovedMandorEvent) event;
        assertEquals(pengiriman.getId(), approved.pengirimanId());
        assertEquals(pengiriman.getTotalKg(), approved.totalKg());
    }

    @Test
    void publishesPengirimanApprovedAdminEventWithRecognizedKg() {
        pengirimanEventPublisher.publishPengirimanApprovedAdmin(pengiriman, 250);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(captor.capture());
        Object event = captor.getValue();
        assertTrue(event instanceof PengirimanApprovedAdminEvent);
        PengirimanApprovedAdminEvent approved = (PengirimanApprovedAdminEvent) event;
        assertEquals(pengiriman.getId(), approved.pengirimanId());
        assertEquals(250, approved.recognizedKg());
    }
}
