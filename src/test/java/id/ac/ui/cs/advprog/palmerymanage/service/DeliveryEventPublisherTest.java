package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.event.PengirimanApprovedAdminEvent;
import id.ac.ui.cs.advprog.palmerymanage.event.PengirimanApprovedMandorEvent;
import id.ac.ui.cs.advprog.palmerymanage.event.PengirimanTibaEvent;
import id.ac.ui.cs.advprog.palmerymanage.model.Delivery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeliveryEventPublisherTest {

    @Mock
    private ApplicationEventPublisher publisher;

    private DeliveryEventPublisher deliveryEventPublisher;

    private Delivery delivery;

    @BeforeEach
    void setUp() {
        deliveryEventPublisher = new DeliveryEventPublisher(publisher);

        delivery = new Delivery();
        delivery.setId(UUID.randomUUID());
        delivery.setSupirId("DRV-1");
        delivery.setMandorId("MDR-1");
        delivery.setTotalKg(300);
        delivery.setPanenIds(List.of("PAN-1", "PAN-2"));
    }

    @Test
    void publishesPengirimanTibaEvent() {
        deliveryEventPublisher.publishPengirimanTiba(delivery);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(captor.capture());
        Object event = captor.getValue();
        assertTrue(event instanceof PengirimanTibaEvent);
        PengirimanTibaEvent tiba = (PengirimanTibaEvent) event;
        assertEquals(delivery.getId(), tiba.pengirimanId());
        assertEquals(delivery.getSupirId(), tiba.supirId());
        assertEquals(delivery.getMandorId(), tiba.mandorId());
        assertEquals(delivery.getTotalKg(), tiba.totalKg());
        assertEquals(delivery.getPanenIds(), tiba.panenIds());
    }

    @Test
    void publishesPengirimanApprovedMandorEvent() {
        deliveryEventPublisher.publishPengirimanApprovedMandor(delivery);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(captor.capture());
        Object event = captor.getValue();
        assertTrue(event instanceof PengirimanApprovedMandorEvent);
        PengirimanApprovedMandorEvent approved = (PengirimanApprovedMandorEvent) event;
        assertEquals(delivery.getId(), approved.pengirimanId());
        assertEquals(delivery.getTotalKg(), approved.totalKg());
    }

    @Test
    void publishesPengirimanApprovedAdminEventWithRecognizedKg() {
        deliveryEventPublisher.publishPengirimanApprovedAdmin(delivery, 250);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(publisher).publishEvent(captor.capture());
        Object event = captor.getValue();
        assertTrue(event instanceof PengirimanApprovedAdminEvent);
        PengirimanApprovedAdminEvent approved = (PengirimanApprovedAdminEvent) event;
        assertEquals(delivery.getId(), approved.pengirimanId());
        assertEquals(250, approved.recognizedKg());
    }
}

