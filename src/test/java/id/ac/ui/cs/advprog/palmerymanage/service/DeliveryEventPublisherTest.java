package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.event.PengirimanApprovedAdminEvent;
import id.ac.ui.cs.advprog.palmerymanage.event.PengirimanApprovedMandorEvent;
import id.ac.ui.cs.advprog.palmerymanage.event.PengirimanTibaEvent;
import id.ac.ui.cs.advprog.palmerymanage.model.Delivery;
import id.ac.ui.cs.advprog.palmerymanage.model.DeliveryStatus;
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
class DeliveryEventPublisherTest {

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private DeliveryEventPublisher deliveryEventPublisher;

    private Delivery delivery;

    @BeforeEach
    void setUp() {
        delivery = new Delivery();
        delivery.setId(UUID.randomUUID());
        delivery.setSupirId("supir-1");
        delivery.setMandorId("mandor-1");
        delivery.setKebunId("kebun-1");
        delivery.setTotalKg(100);
        delivery.setPanenIds(List.of("panen-1", "panen-2"));
        delivery.setStatus(DeliveryStatus.MEMUAT);
    }

    @Test
    void publishPengirimanTiba_publishesEvent() {
        deliveryEventPublisher.publishPengirimanTiba(delivery);

        ArgumentCaptor<PengirimanTibaEvent> captor = ArgumentCaptor.forClass(PengirimanTibaEvent.class);
        verify(publisher, times(1)).publishEvent(captor.capture());

        PengirimanTibaEvent event = captor.getValue();
        assertEquals(delivery.getId(), event.pengirimanId());
        assertEquals("supir-1", event.supirId());
        assertEquals("mandor-1", event.mandorId());
        assertEquals(100, event.totalKg());
        assertEquals(List.of("panen-1", "panen-2"), event.panenIds());
        assertNotNull(event.timestamp());
    }

    @Test
    void publishPengirimanApprovedMandor_publishesEvent() {
        deliveryEventPublisher.publishPengirimanApprovedMandor(delivery);

        ArgumentCaptor<PengirimanApprovedMandorEvent> captor = ArgumentCaptor.forClass(PengirimanApprovedMandorEvent.class);
        verify(publisher, times(1)).publishEvent(captor.capture());

        PengirimanApprovedMandorEvent event = captor.getValue();
        assertEquals(delivery.getId(), event.pengirimanId());
        assertEquals("supir-1", event.supirId());
        assertEquals(100, event.totalKg());
    }

    @Test
    void publishPengirimanApprovedAdmin_publishesEvent() {
        deliveryEventPublisher.publishPengirimanApprovedAdmin(delivery, 80);

        ArgumentCaptor<PengirimanApprovedAdminEvent> captor = ArgumentCaptor.forClass(PengirimanApprovedAdminEvent.class);
        verify(publisher, times(1)).publishEvent(captor.capture());

        PengirimanApprovedAdminEvent event = captor.getValue();
        assertEquals(delivery.getId(), event.pengirimanId());
        assertEquals(100, event.totalKg());
        assertEquals(80, event.recognizedKg());
    }
}
