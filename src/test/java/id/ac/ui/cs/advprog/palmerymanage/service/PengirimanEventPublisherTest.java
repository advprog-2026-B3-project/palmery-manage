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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PengirimanEventPublisherTest {

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @Mock
    private id.ac.ui.cs.advprog.palmerymanage.event.DomainEventPublisher domainEventPublisher;

    private PengirimanEventPublisher pengirimanEventPublisher;

    private Pengiriman pengiriman;

    @BeforeEach
    void setUp() {
        pengirimanEventPublisher = new SpringPengirimanEventPublisher(publisher, domainEventPublisher);

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

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(captor.capture());
        Object event = captor.getValue();
        assertTrue(event instanceof PengirimanApprovedMandorEvent);
        PengirimanApprovedMandorEvent approved = (PengirimanApprovedMandorEvent) event;
        assertEquals(pengiriman.getId(), approved.pengirimanId());
        assertEquals(pengiriman.getTotalKg(), approved.totalKg());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> payloadCaptor =
            ArgumentCaptor.forClass((Class<Map<String, Object>>) (Class<?>) Map.class);
        verify(domainEventPublisher).publish(eq("PENGIRIMAN_APPROVED_BY_MANDOR"), payloadCaptor.capture());

        Map<String, Object> payload = (Map<String, Object>) payloadCaptor.getValue();
        assertEquals(pengiriman.getId().toString(), payload.get("pengirimanId"));
        assertEquals(pengiriman.getSupirId(), payload.get("supirId"));
        assertEquals(pengiriman.getMandorId(), payload.get("mandorId"));
        assertEquals(pengiriman.getSupirId(), payload.get("userId"));
        assertEquals(BigDecimal.valueOf(pengiriman.getTotalKg()), payload.get("quantityKg"));
        assertEquals(pengiriman.getTotalKg(), payload.get("totalKg"));
        assertEquals(pengiriman.getPanenIds(), payload.get("panenIds"));
        assertEquals("Pengiriman disetujui mandor untuk " + pengiriman.getTotalKg() + " Kg", payload.get("description"));
        assertEquals("Pengiriman disetujui mandor", payload.get("title"));
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
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> payloadCaptor =
            ArgumentCaptor.forClass((Class<Map<String, Object>>) (Class<?>) Map.class);
        verify(domainEventPublisher).publish(eq("PENGIRIMAN_APPROVED_BY_ADMIN"), payloadCaptor.capture());

        Map<String, Object> payload = (Map<String, Object>) payloadCaptor.getValue();
        assertEquals(pengiriman.getId().toString(), payload.get("pengirimanId"));
        assertEquals(pengiriman.getSupirId(), payload.get("supirId"));
        assertEquals(pengiriman.getMandorId(), payload.get("mandorId"));
        assertEquals(pengiriman.getMandorId(), payload.get("userId"));
        assertEquals(BigDecimal.valueOf(250), payload.get("quantityKg"));
        assertEquals(250, payload.get("recognizedKg"));
        assertEquals(250, payload.get("acceptedKgByAdmin"));
        assertEquals(pengiriman.getTotalKg(), payload.get("totalKg"));
        assertEquals(pengiriman.getPanenIds(), payload.get("panenIds"));
        assertEquals("Pengiriman diakui admin untuk 250 Kg", payload.get("description"));
        assertEquals("Pengiriman disetujui admin", payload.get("title"));
    }
}
