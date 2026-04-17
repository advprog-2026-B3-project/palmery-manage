package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.dto.CreatePengirimanRequest;
import id.ac.ui.cs.advprog.palmerymanage.exception.BadRequestException;
import id.ac.ui.cs.advprog.palmerymanage.exception.ForbiddenException;
import id.ac.ui.cs.advprog.palmerymanage.exception.OverWeightException;
import id.ac.ui.cs.advprog.palmerymanage.model.Delivery;
import id.ac.ui.cs.advprog.palmerymanage.model.DeliveryStatus;
import id.ac.ui.cs.advprog.palmerymanage.model.Harvest;
import id.ac.ui.cs.advprog.palmerymanage.model.Mandor;
import id.ac.ui.cs.advprog.palmerymanage.repository.DeliveryRepository;
import id.ac.ui.cs.advprog.palmerymanage.repository.HarvestRepository;
import id.ac.ui.cs.advprog.palmerymanage.repository.MandorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private HarvestRepository harvestRepository;

    @Mock
    private MandorRepository mandorRepository;

    @Mock
    private DeliveryEventPublisher eventPublisher;

    @InjectMocks
    private DeliveryService deliveryService;

    private Mandor mandor;

    @BeforeEach
    void setUp() {
        mandor = new Mandor();
        mandor.setId("MDR-1");
        mandor.setKebunId("KEB-1");
        mandor.setNama("Slamet");
    }

    @Test
    void createPengirimanWithinLimitSucceeds() {
        CreatePengirimanRequest request = new CreatePengirimanRequest("DRV-1", List.of("PAN-1", "PAN-2"));

        Harvest h1 = new Harvest();
        h1.setId("PAN-1");
        h1.setBeratKg(200);
        h1.setReadyForDelivery(true);

        Harvest h2 = new Harvest();
        h2.setId("PAN-2");
        h2.setBeratKg(200);
        h2.setReadyForDelivery(true);

        when(mandorRepository.findById("MDR-1")).thenReturn(Optional.of(mandor));
        when(harvestRepository.findAllById(request.panenIds())).thenReturn(List.of(h1, h2));

        ArgumentCaptor<Delivery> captor = ArgumentCaptor.forClass(Delivery.class);
        when(deliveryRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        Delivery result = deliveryService.createPengiriman("MDR-1", request);

        assertEquals(400, result.getTotalKg());
        assertEquals(DeliveryStatus.MEMUAT, result.getStatus());
        assertEquals("DRV-1", result.getSupirId());
        verify(deliveryRepository).save(result);
    }

    @Test
    void createPengirimanOverLimitThrows() {
        CreatePengirimanRequest request = new CreatePengirimanRequest("DRV-1", List.of("PAN-1", "PAN-2"));

        Harvest h1 = new Harvest();
        h1.setId("PAN-1");
        h1.setBeratKg(300);
        h1.setReadyForDelivery(true);

        Harvest h2 = new Harvest();
        h2.setId("PAN-2");
        h2.setBeratKg(200);
        h2.setReadyForDelivery(true);

        when(mandorRepository.findById("MDR-1")).thenReturn(Optional.of(mandor));
        when(harvestRepository.findAllById(request.panenIds())).thenReturn(List.of(h1, h2));

        assertThrows(OverWeightException.class, () -> deliveryService.createPengiriman("MDR-1", request));
    }

    @Test
    void driverStateMachineValidTransitions() {
        Delivery delivery = new Delivery();
        delivery.setId(UUID.randomUUID());
        delivery.setSupirId("DRV-1");
        delivery.setStatus(DeliveryStatus.MEMUAT);

        when(deliveryRepository.findById(delivery.getId())).thenReturn(Optional.of(delivery));

        Delivery afterFirst = deliveryService.updateStatusSupir("DRV-1", delivery.getId(), DeliveryStatus.MENGIRIM);
        assertEquals(DeliveryStatus.MENGIRIM, afterFirst.getStatus());

        when(deliveryRepository.findById(delivery.getId())).thenReturn(Optional.of(afterFirst));
        Delivery afterSecond = deliveryService.updateStatusSupir("DRV-1", delivery.getId(), DeliveryStatus.TIBA_DI_TUJUAN);
        assertNotNull(afterSecond.getStatus());
        verify(eventPublisher).publishPengirimanTiba(afterSecond);
    }

    @Test
    void driverStateMachineInvalidJumpThrows() {
        Delivery delivery = new Delivery();
        delivery.setId(UUID.randomUUID());
        delivery.setSupirId("DRV-1");
        delivery.setStatus(DeliveryStatus.MEMUAT);

        when(deliveryRepository.findById(delivery.getId())).thenReturn(Optional.of(delivery));

        assertThrows(BadRequestException.class,
                () -> deliveryService.updateStatusSupir("DRV-1", delivery.getId(), DeliveryStatus.TIBA_DI_TUJUAN));
    }

    @Test
    void driverCannotUpdateOtherDriversDelivery() {
        Delivery delivery = new Delivery();
        delivery.setId(UUID.randomUUID());
        delivery.setSupirId("DRV-OTHER");
        delivery.setStatus(DeliveryStatus.MEMUAT);

        when(deliveryRepository.findById(delivery.getId())).thenReturn(Optional.of(delivery));

        assertThrows(ForbiddenException.class,
                () -> deliveryService.updateStatusSupir("DRV-1", delivery.getId(), DeliveryStatus.MENGIRIM));
    }

    @Test
    void adminApproveEmitsEvent() {
        Delivery delivery = new Delivery();
        delivery.setId(UUID.randomUUID());
        delivery.setSupirId("DRV-1");
        delivery.setMandorId("MDR-1");
        delivery.setStatus(DeliveryStatus.PENDING_ADMIN_REVIEW);
        delivery.setTotalKg(300);

        when(deliveryRepository.findById(delivery.getId())).thenReturn(Optional.of(delivery));

        Delivery result = deliveryService.approveByAdmin(delivery.getId());

        assertEquals(DeliveryStatus.APPROVED_ADMIN, result.getStatus());
        verify(eventPublisher).publishPengirimanApprovedAdmin(delivery, 300);
    }

    @Test
    void partialRejectValidatesRecognizedKg() {
        Delivery delivery = new Delivery();
        delivery.setId(UUID.randomUUID());
        delivery.setTotalKg(300);

        when(deliveryRepository.findById(delivery.getId())).thenReturn(Optional.of(delivery));

        assertThrows(BadRequestException.class,
                () -> deliveryService.partialRejectByAdmin(delivery.getId(), 0, "kurang timbang"));

        Delivery ok = deliveryService.partialRejectByAdmin(delivery.getId(), 200, "susut");

        assertEquals(DeliveryStatus.PARTIAL_REJECTED_ADMIN, ok.getStatus());
        assertEquals(200, ok.getRecognizedKg());
        verify(eventPublisher).publishPengirimanApprovedAdmin(delivery, 200);
    }

    @Test
    void historyUsesDateRange() {
        deliveryService.riwayatSupir("DRV-1", LocalDate.now().minusDays(7), LocalDate.now());
        verify(deliveryRepository).findBySupirIdAndCreatedAtBetween(org.mockito.ArgumentMatchers.eq("DRV-1"),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}

