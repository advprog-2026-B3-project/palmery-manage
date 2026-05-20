package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.dto.CreatePengirimanRequest;
import id.ac.ui.cs.advprog.palmerymanage.exception.BadRequestException;
import id.ac.ui.cs.advprog.palmerymanage.exception.ForbiddenException;
import id.ac.ui.cs.advprog.palmerymanage.exception.OverWeightException;
import id.ac.ui.cs.advprog.palmerymanage.model.Delivery;
import id.ac.ui.cs.advprog.palmerymanage.model.DeliveryStatus;
import id.ac.ui.cs.advprog.palmerymanage.model.HarvestResult;
import id.ac.ui.cs.advprog.palmerymanage.model.Plantation;
import id.ac.ui.cs.advprog.palmerymanage.repository.DeliveryRepository;
import id.ac.ui.cs.advprog.palmerymanage.repository.HarvestResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private HarvestResultRepository harvestResultRepository;

    @Mock
    private DeliveryEventPublisher eventPublisher;

    @InjectMocks
    private DeliveryService deliveryService;

    private UUID harvestId1;
    private UUID harvestId2;
    private UUID deliveryId;
    private String mandorId;
    private String supirId;
    private HarvestResult harvest1;
    private HarvestResult harvest2;
    private Delivery delivery;

    @BeforeEach
    void setUp() {
        harvestId1 = UUID.randomUUID();
        harvestId2 = UUID.randomUUID();
        deliveryId = UUID.randomUUID();
        mandorId = UUID.randomUUID().toString();
        supirId = "supir-1";

        Plantation plantation = new Plantation();
        plantation.setId(UUID.randomUUID());

        harvest1 = HarvestResult.builder()
                .workerId(UUID.randomUUID())
                .mandorId(UUID.fromString(mandorId))
                .plantation(plantation)
                .harvestDate(LocalDate.now())
                .kgHarvested(100f)
                .notes("test")
                .readyForDelivery(true)
                .status("APPROVED")
                .build();
        harvest1.setId(harvestId1);

        harvest2 = HarvestResult.builder()
                .workerId(UUID.randomUUID())
                .mandorId(UUID.fromString(mandorId))
                .plantation(plantation)
                .harvestDate(LocalDate.now())
                .kgHarvested(50f)
                .notes("test")
                .readyForDelivery(true)
                .status("APPROVED")
                .build();
        harvest2.setId(harvestId2);

        delivery = new Delivery();
        delivery.setId(deliveryId);
        delivery.setSupirId(supirId);
        delivery.setMandorId(mandorId);
        delivery.setKebunId(plantation.getId().toString());
        delivery.setTotalKg(150);
        delivery.setPanenIds(List.of(harvestId1.toString(), harvestId2.toString()));
        delivery.setStatus(DeliveryStatus.MEMUAT);
    }

    // === createPengiriman ===

    @Test
    void createPengiriman_success() {
        CreatePengirimanRequest request = new CreatePengirimanRequest(supirId, List.of(harvestId1.toString(), harvestId2.toString()));
        when(harvestResultRepository.findAllById(anyList())).thenReturn(List.of(harvest1, harvest2));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(inv -> inv.getArgument(0));

        Delivery result = deliveryService.createPengiriman(mandorId, request);

        assertNotNull(result);
        assertEquals(DeliveryStatus.MEMUAT, result.getStatus());
        verify(harvestResultRepository).saveAll(anyList());
        verify(deliveryRepository).save(any(Delivery.class));
    }

    @Test
    void createPengiriman_nullMandorId_throwsBadRequest() {
        CreatePengirimanRequest request = new CreatePengirimanRequest(supirId, List.of(harvestId1.toString()));
        assertThrows(BadRequestException.class, () -> deliveryService.createPengiriman(null, request));
    }

    @Test
    void createPengiriman_blankMandorId_throwsBadRequest() {
        CreatePengirimanRequest request = new CreatePengirimanRequest(supirId, List.of(harvestId1.toString()));
        assertThrows(BadRequestException.class, () -> deliveryService.createPengiriman("   ", request));
    }

    @Test
    void createPengiriman_invalidUuid_throwsBadRequest() {
        CreatePengirimanRequest request = new CreatePengirimanRequest(supirId, List.of("not-a-uuid"));
        assertThrows(BadRequestException.class, () -> deliveryService.createPengiriman(mandorId, request));
    }

    @Test
    void createPengiriman_missingHarvest_throwsBadRequest() {
        CreatePengirimanRequest request = new CreatePengirimanRequest(supirId, List.of(harvestId1.toString(), harvestId2.toString()));
        when(harvestResultRepository.findAllById(anyList())).thenReturn(List.of(harvest1));
        assertThrows(BadRequestException.class, () -> deliveryService.createPengiriman(mandorId, request));
    }

    @Test
    void createPengiriman_notReadyForDelivery_throwsBadRequest() {
        harvest1.setReadyForDelivery(false);
        CreatePengirimanRequest request = new CreatePengirimanRequest(supirId, List.of(harvestId1.toString()));
        when(harvestResultRepository.findAllById(anyList())).thenReturn(List.of(harvest1));
        assertThrows(BadRequestException.class, () -> deliveryService.createPengiriman(mandorId, request));
    }

    @Test
    void createPengiriman_overWeight_throwsOverWeightException() {
        harvest1.setKgHarvested(401f);
        CreatePengirimanRequest request = new CreatePengirimanRequest(supirId, List.of(harvestId1.toString()));
        when(harvestResultRepository.findAllById(anyList())).thenReturn(List.of(harvest1));
        assertThrows(OverWeightException.class, () -> deliveryService.createPengiriman(mandorId, request));
    }

    @Test
    void createPengiriman_notOwnedByMandor_throwsForbidden() {
        harvest1.setMandorId(UUID.randomUUID());
        CreatePengirimanRequest request = new CreatePengirimanRequest(supirId, List.of(harvestId1.toString()));
        when(harvestResultRepository.findAllById(anyList())).thenReturn(List.of(harvest1));
        assertThrows(ForbiddenException.class, () -> deliveryService.createPengiriman(mandorId, request));
    }

    // === pengirimanAktifSupir ===

    @Test
    void pengirimanAktifSupir_returnsActiveDeliveries() {
        when(deliveryRepository.findBySupirIdAndStatusIn(eq(supirId), anyList())).thenReturn(List.of(delivery));
        List<Delivery> result = deliveryService.pengirimanAktifSupir(supirId);
        assertEquals(1, result.size());
    }

    // === riwayatSupir ===

    @Test
    void riwayatSupir_returnsDeliveriesInRange() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 12, 31);
        when(deliveryRepository.findBySupirIdAndCreatedAtBetween(eq(supirId), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(delivery));
        List<Delivery> result = deliveryService.riwayatSupir(supirId, from, to);
        assertEquals(1, result.size());
    }

    // === pengirimanAktifKebun ===

    @Test
    void pengirimanAktifKebun_returnsDeliveries() {
        when(deliveryRepository.findByKebunIdAndStatusIn(eq("kebun-1"), anyList())).thenReturn(List.of(delivery));
        List<Delivery> result = deliveryService.pengirimanAktifKebun("kebun-1");
        assertEquals(1, result.size());
    }

    // === updateStatusSupir ===

    @Test
    void updateStatusSupir_memuatToMengirim_success() {
        delivery.setStatus(DeliveryStatus.MEMUAT);
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));

        Delivery result = deliveryService.updateStatusSupir(supirId, deliveryId, DeliveryStatus.MENGIRIM);
        assertEquals(DeliveryStatus.MENGIRIM, result.getStatus());
    }

    @Test
    void updateStatusSupir_mengirimToTiba_triggersEvent() {
        delivery.setStatus(DeliveryStatus.MENGIRIM);
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));

        Delivery result = deliveryService.updateStatusSupir(supirId, deliveryId, DeliveryStatus.TIBA_DI_TUJUAN);
        assertEquals(DeliveryStatus.PENDING_MANDOR_REVIEW, result.getStatus());
        verify(eventPublisher).publishPengirimanTiba(any(Delivery.class));
    }

    @Test
    void updateStatusSupir_notFound_throwsBadRequest() {
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.empty());
        assertThrows(BadRequestException.class, () -> deliveryService.updateStatusSupir(supirId, deliveryId, DeliveryStatus.MENGIRIM));
    }

    @Test
    void updateStatusSupir_notOwnedBySupir_throwsForbidden() {
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));
        assertThrows(ForbiddenException.class, () -> deliveryService.updateStatusSupir("other-supir", deliveryId, DeliveryStatus.MENGIRIM));
    }

    @Test
    void updateStatusSupir_invalidTransition_throwsBadRequest() {
        delivery.setStatus(DeliveryStatus.APPROVED_ADMIN);
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));
        assertThrows(BadRequestException.class, () -> deliveryService.updateStatusSupir(supirId, deliveryId, DeliveryStatus.MENGIRIM));
    }

    // === isValidTransitionForDriver ===

    @Test
    void isValidTransitionForDriver_memuatToMengirim_true() {
        assertTrue(deliveryService.isValidTransitionForDriver(DeliveryStatus.MEMUAT, DeliveryStatus.MENGIRIM));
    }

    @Test
    void isValidTransitionForDriver_mengirimToTiba_true() {
        assertTrue(deliveryService.isValidTransitionForDriver(DeliveryStatus.MENGIRIM, DeliveryStatus.TIBA_DI_TUJUAN));
    }

    @Test
    void isValidTransitionForDriver_invalidTransition_false() {
        assertFalse(deliveryService.isValidTransitionForDriver(DeliveryStatus.MEMUAT, DeliveryStatus.TIBA_DI_TUJUAN));
        assertFalse(deliveryService.isValidTransitionForDriver(DeliveryStatus.APPROVED_ADMIN, DeliveryStatus.MENGIRIM));
    }

    // === approveByMandor ===

    @Test
    void approveByMandor_success() {
        delivery.setStatus(DeliveryStatus.PENDING_MANDOR_REVIEW);
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));

        Delivery result = deliveryService.approveByMandor(mandorId, deliveryId);
        assertEquals(DeliveryStatus.PENDING_ADMIN_REVIEW, result.getStatus());
        verify(eventPublisher).publishPengirimanApprovedMandor(any(Delivery.class));
    }

    @Test
    void approveByMandor_notPendingReview_throwsBadRequest() {
        delivery.setStatus(DeliveryStatus.MEMUAT);
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));
        assertThrows(BadRequestException.class, () -> deliveryService.approveByMandor(mandorId, deliveryId));
    }

    @Test
    void approveByMandor_notOwnedByMandor_throwsForbidden() {
        delivery.setStatus(DeliveryStatus.PENDING_MANDOR_REVIEW);
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));
        assertThrows(ForbiddenException.class, () -> deliveryService.approveByMandor("other-mandor", deliveryId));
    }

    // === rejectByMandor ===

    @Test
    void rejectByMandor_success() {
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));

        Delivery result = deliveryService.rejectByMandor(mandorId, deliveryId, "Alasan reject");
        assertEquals(DeliveryStatus.REJECTED_MANDOR, result.getStatus());
        assertEquals("Alasan reject", result.getRejectedReason());
    }

    // === pendingAdmin ===

    @Test
    void pendingAdmin_returnsList() {
        when(deliveryRepository.findByStatus(DeliveryStatus.PENDING_ADMIN_REVIEW)).thenReturn(List.of(delivery));
        List<Delivery> result = deliveryService.pendingAdmin();
        assertEquals(1, result.size());
    }

    // === getById ===

    @Test
    void getById_success() {
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));
        Delivery result = deliveryService.getById(deliveryId);
        assertNotNull(result);
    }

    @Test
    void getById_notFound_throwsBadRequest() {
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.empty());
        assertThrows(BadRequestException.class, () -> deliveryService.getById(deliveryId));
    }

    // === approveByAdmin ===

    @Test
    void approveByAdmin_success() {
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));

        Delivery result = deliveryService.approveByAdmin(deliveryId);
        assertEquals(DeliveryStatus.APPROVED_ADMIN, result.getStatus());
        verify(eventPublisher).publishPengirimanApprovedAdmin(any(Delivery.class), eq(150));
    }

    // === rejectByAdmin ===

    @Test
    void rejectByAdmin_success() {
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));

        Delivery result = deliveryService.rejectByAdmin(deliveryId, "Reject reason");
        assertEquals(DeliveryStatus.REJECTED_ADMIN, result.getStatus());
        assertEquals("Reject reason", result.getRejectedReason());
    }

    // === partialRejectByAdmin ===

    @Test
    void partialRejectByAdmin_success() {
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));

        Delivery result = deliveryService.partialRejectByAdmin(deliveryId, 100, "Partial reason");
        assertEquals(DeliveryStatus.PARTIAL_REJECTED_ADMIN, result.getStatus());
        assertEquals(100, result.getRecognizedKg());
        assertEquals("Partial reason", result.getRejectedReason());
        verify(eventPublisher).publishPengirimanApprovedAdmin(any(Delivery.class), eq(100));
    }

    @Test
    void partialRejectByAdmin_invalidKgZero_throwsBadRequest() {
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));
        assertThrows(BadRequestException.class, () -> deliveryService.partialRejectByAdmin(deliveryId, 0, "reason"));
    }

    @Test
    void partialRejectByAdmin_invalidKgOverTotal_throwsBadRequest() {
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));
        assertThrows(BadRequestException.class, () -> deliveryService.partialRejectByAdmin(deliveryId, 999, "reason"));
    }
}
