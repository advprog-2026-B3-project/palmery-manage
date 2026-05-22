package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.dto.CreatePengirimanRequest;
import id.ac.ui.cs.advprog.palmerymanage.exception.BadRequestException;
import id.ac.ui.cs.advprog.palmerymanage.exception.ForbiddenException;
import id.ac.ui.cs.advprog.palmerymanage.exception.OverWeightException;
import id.ac.ui.cs.advprog.palmerymanage.model.HarvestResult;
import id.ac.ui.cs.advprog.palmerymanage.model.Pengiriman;
import id.ac.ui.cs.advprog.palmerymanage.model.PengirimanStatus;
import id.ac.ui.cs.advprog.palmerymanage.model.Plantation;
import id.ac.ui.cs.advprog.palmerymanage.model.PlantationAssignment;
import id.ac.ui.cs.advprog.palmerymanage.pengiriman.DriverDirectoryLookup;
import id.ac.ui.cs.advprog.palmerymanage.pengiriman.DriverProfileLookup;
import id.ac.ui.cs.advprog.palmerymanage.repository.HarvestResultRepository;
import id.ac.ui.cs.advprog.palmerymanage.repository.PengirimanRepository;
import id.ac.ui.cs.advprog.palmerymanage.repository.PlantationAssignmentRepository;
import id.ac.ui.cs.advprog.palmerymanage.pengiriman.PengirimanEventPublisher;
import id.ac.ui.cs.advprog.palmerymanage.pengiriman.PengirimanStatusTransitionPolicy;
import id.ac.ui.cs.advprog.palmerymanage.repository.PlantationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PengirimanServiceTest {

    @Mock
    private PengirimanRepository pengirimanRepository;

    @Mock
    private HarvestResultRepository harvestResultRepository;

    @Mock
    private PlantationAssignmentRepository plantationAssignmentRepository;

    @Mock
    private PlantationRepository plantationRepository;

    @Mock
    private PengirimanEventPublisher eventPublisher;

    @Mock
    private PengirimanStatusTransitionPolicy statusTransitionPolicy;

    @Mock
    private DriverProfileLookup driverProfileLookup;

    @Mock
    private DriverDirectoryLookup driverDirectoryLookup;

    @Mock
    private Environment environment;

    private PengirimanService pengirimanService;

    private UUID harvestId1;
    private UUID harvestId2;
    private UUID pengirimanId;
    private UUID mandorUuid;
    private UUID supirUuid;
    private UUID kebunId;
    private String mandorId;
    private String supirId;
    private HarvestResult harvest1;
    private HarvestResult harvest2;
    private Pengiriman pengiriman;

    @BeforeEach
    void setUp() {
        harvestId1 = UUID.randomUUID();
        harvestId2 = UUID.randomUUID();
        pengirimanId = UUID.randomUUID();
        mandorUuid = UUID.randomUUID();
        supirUuid = UUID.randomUUID();
        kebunId = UUID.randomUUID();
        mandorId = mandorUuid.toString();
        supirId = supirUuid.toString();
        pengirimanService = new PengirimanService(
                pengirimanRepository,
                harvestResultRepository,
                plantationAssignmentRepository,
                plantationRepository,
                eventPublisher,
                statusTransitionPolicy,
                driverProfileLookup,
                driverDirectoryLookup,
                environment
        );

        harvest1 = new HarvestResult();
        harvest1.setId(harvestId1);
        harvest1.setWorkerId(UUID.randomUUID());
        harvest1.setMandorId(mandorUuid);
        Plantation p1 = new Plantation();
        p1.setId(kebunId);
        harvest1.setPlantation(p1);
        harvest1.setHarvestDate(LocalDate.now());
        harvest1.setKgHarvested(100f);
        harvest1.setNotes("test");
        harvest1.setReadyForDelivery(true);
        harvest1.setStatus("APPROVED");

        harvest2 = new HarvestResult();
        harvest2.setId(harvestId2);
        harvest2.setWorkerId(UUID.randomUUID());
        harvest2.setMandorId(mandorUuid);
        Plantation p2 = new Plantation();
        p2.setId(kebunId);
        harvest2.setPlantation(p2);
        harvest2.setHarvestDate(LocalDate.now());
        harvest2.setKgHarvested(50f);
        harvest2.setNotes("test");
        harvest2.setReadyForDelivery(true);
        harvest2.setStatus("APPROVED");

        pengiriman = new Pengiriman();
        pengiriman.setId(pengirimanId);
        pengiriman.setSupirId(supirId);
        pengiriman.setMandorId(mandorId);
        pengiriman.setKebunId(kebunId.toString());
        pengiriman.setTotalKg(150);
        pengiriman.setPanenIds(List.of(harvestId1.toString(), harvestId2.toString()));
        pengiriman.setStatus(PengirimanStatus.MEMUAT);

        lenient().when(pengirimanRepository.save(any(Pengiriman.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private PlantationAssignment createAssignment(UUID personnelId, PlantationAssignment.PersonnelRole role) {
        PlantationAssignment a = new PlantationAssignment();
        a.setPersonnelId(personnelId);
        a.setRole(role);
        a.setPlantationId(kebunId);
        return a;
    }

    @Test
    void listSupirOnKebunMandor_success() {
        when(plantationAssignmentRepository.findByPersonnelIdAndRole(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR))
                .thenReturn(List.of(createAssignment(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR)));
        when(plantationAssignmentRepository.findByPlantationIdAndRole(kebunId, PlantationAssignment.PersonnelRole.SUPIR))
                .thenReturn(List.of(createAssignment(supirUuid, PlantationAssignment.PersonnelRole.SUPIR)));
        when(driverProfileLookup.fetchUsersByIds(List.of(supirUuid)))
                .thenReturn(Map.of(supirUuid, new AuthUserClient.UserSummary(supirUuid, "Nama Supir", "supir1@example.com")));

        List<Map<String, Object>> result = pengirimanService.listSupirOnKebunMandor(mandorId, null);
        assertEquals(1, result.size());
        assertEquals(supirId, result.get(0).get("id"));
        assertEquals("Nama Supir", result.get(0).get("nama"));
    }

    @Test
    void listSupirOnKebunMandor_withSearch() {
        when(plantationAssignmentRepository.findByPersonnelIdAndRole(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR))
                .thenReturn(List.of(createAssignment(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR)));
        when(plantationAssignmentRepository.findByPlantationIdAndRole(kebunId, PlantationAssignment.PersonnelRole.SUPIR))
                .thenReturn(List.of(createAssignment(supirUuid, PlantationAssignment.PersonnelRole.SUPIR)));
        when(driverProfileLookup.fetchUsersByIds(List.of(supirUuid)))
                .thenReturn(Map.of(supirUuid, new AuthUserClient.UserSummary(supirUuid, "Nama Supir", "supir1@example.com")));

        List<Map<String, Object>> result1 = pengirimanService.listSupirOnKebunMandor(mandorId, "nama");
        assertEquals(1, result1.size());

        List<Map<String, Object>> result2 = pengirimanService.listSupirOnKebunMandor(mandorId, "wrong");
        assertEquals(0, result2.size());
    }

    @Test
    void createPengiriman_success() {
        CreatePengirimanRequest request = new CreatePengirimanRequest(supirId, List.of(harvestId1.toString(), harvestId2.toString()));

        when(plantationAssignmentRepository.findByPersonnelIdAndRole(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR))
                .thenReturn(List.of(createAssignment(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR)));
        when(plantationAssignmentRepository.existsByPlantationIdAndPersonnelIdAndRole(kebunId, supirUuid, PlantationAssignment.PersonnelRole.SUPIR))
                .thenReturn(true);
        when(harvestResultRepository.findAllById(anyList())).thenReturn(List.of(harvest1, harvest2));
        when(pengirimanRepository.save(any(Pengiriman.class))).thenAnswer(inv -> inv.getArgument(0));

        Pengiriman result = pengirimanService.createPengiriman(mandorId, request);

        assertNotNull(result);
        assertEquals(PengirimanStatus.MEMUAT, result.getStatus());
        verify(harvestResultRepository).saveAll(anyList());
        verify(pengirimanRepository).save(any(Pengiriman.class));
    }

    @Test
    void createPengiriman_nullMandorId_throwsBadRequest() {
        CreatePengirimanRequest request = new CreatePengirimanRequest(supirId, List.of(harvestId1.toString()));
        assertThrows(BadRequestException.class, () -> pengirimanService.createPengiriman(null, request));
    }

    @Test
    void createPengiriman_blankMandorId_throwsBadRequest() {
        CreatePengirimanRequest request = new CreatePengirimanRequest(supirId, List.of(harvestId1.toString()));
        assertThrows(BadRequestException.class, () -> pengirimanService.createPengiriman("   ", request));
    }

    @Test
    void createPengiriman_invalidPanenId_throwsBadRequest() {
        when(plantationAssignmentRepository.findByPersonnelIdAndRole(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR))
                .thenReturn(List.of(createAssignment(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR)));
        when(plantationAssignmentRepository.existsByPlantationIdAndPersonnelIdAndRole(kebunId, supirUuid, PlantationAssignment.PersonnelRole.SUPIR))
                .thenReturn(true);

        CreatePengirimanRequest request = new CreatePengirimanRequest(supirId, List.of("not-a-uuid"));
        assertThrows(BadRequestException.class, () -> pengirimanService.createPengiriman(mandorId, request));
    }

    @Test
    void createPengiriman_missingHarvest_throwsBadRequest() {
        when(plantationAssignmentRepository.findByPersonnelIdAndRole(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR))
                .thenReturn(List.of(createAssignment(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR)));
        when(plantationAssignmentRepository.existsByPlantationIdAndPersonnelIdAndRole(kebunId, supirUuid, PlantationAssignment.PersonnelRole.SUPIR))
                .thenReturn(true);

        CreatePengirimanRequest request = new CreatePengirimanRequest(supirId, List.of(harvestId1.toString(), harvestId2.toString()));
        when(harvestResultRepository.findAllById(anyList())).thenReturn(List.of(harvest1));
        assertThrows(BadRequestException.class, () -> pengirimanService.createPengiriman(mandorId, request));
    }

    @Test
    void createPengiriman_notReadyForDelivery_throwsBadRequest() {
        when(plantationAssignmentRepository.findByPersonnelIdAndRole(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR))
                .thenReturn(List.of(createAssignment(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR)));
        when(plantationAssignmentRepository.existsByPlantationIdAndPersonnelIdAndRole(kebunId, supirUuid, PlantationAssignment.PersonnelRole.SUPIR))
                .thenReturn(true);

        harvest1.setReadyForDelivery(false);
        CreatePengirimanRequest request = new CreatePengirimanRequest(supirId, List.of(harvestId1.toString()));
        when(harvestResultRepository.findAllById(anyList())).thenReturn(List.of(harvest1));
        assertThrows(BadRequestException.class, () -> pengirimanService.createPengiriman(mandorId, request));
    }

    @Test
    void createPengiriman_overWeight_throwsOverWeightException() {
        when(plantationAssignmentRepository.findByPersonnelIdAndRole(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR))
                .thenReturn(List.of(createAssignment(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR)));
        when(plantationAssignmentRepository.existsByPlantationIdAndPersonnelIdAndRole(kebunId, supirUuid, PlantationAssignment.PersonnelRole.SUPIR))
                .thenReturn(true);

        harvest1.setKgHarvested(401f);
        CreatePengirimanRequest request = new CreatePengirimanRequest(supirId, List.of(harvestId1.toString()));
        when(harvestResultRepository.findAllById(anyList())).thenReturn(List.of(harvest1));
        assertThrows(OverWeightException.class, () -> pengirimanService.createPengiriman(mandorId, request));
    }

    @Test
    void createPengiriman_notOwnedByMandor_throwsForbidden() {
        when(plantationAssignmentRepository.findByPersonnelIdAndRole(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR))
                .thenReturn(List.of(createAssignment(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR)));
        when(plantationAssignmentRepository.existsByPlantationIdAndPersonnelIdAndRole(kebunId, supirUuid, PlantationAssignment.PersonnelRole.SUPIR))
                .thenReturn(true);

        harvest1.setMandorId(UUID.randomUUID());
        CreatePengirimanRequest request = new CreatePengirimanRequest(supirId, List.of(harvestId1.toString()));
        when(harvestResultRepository.findAllById(anyList())).thenReturn(List.of(harvest1));
        assertThrows(ForbiddenException.class, () -> pengirimanService.createPengiriman(mandorId, request));
    }

    @Test
    void createPengiriman_wrongKebun_throwsBadRequest() {
        when(plantationAssignmentRepository.findByPersonnelIdAndRole(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR))
                .thenReturn(List.of(createAssignment(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR)));
        when(plantationAssignmentRepository.existsByPlantationIdAndPersonnelIdAndRole(kebunId, supirUuid, PlantationAssignment.PersonnelRole.SUPIR))
                .thenReturn(true);

        Plantation wrongPlantation = new Plantation();
        wrongPlantation.setId(UUID.randomUUID());
        harvest1.setPlantation(wrongPlantation);
        CreatePengirimanRequest request = new CreatePengirimanRequest(supirId, List.of(harvestId1.toString()));
        when(harvestResultRepository.findAllById(anyList())).thenReturn(List.of(harvest1));
        assertThrows(BadRequestException.class, () -> pengirimanService.createPengiriman(mandorId, request));
    }

    @Test
    void createPengiriman_supirNotOnKebun_throwsBadRequest() {
        when(plantationAssignmentRepository.findByPersonnelIdAndRole(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR))
                .thenReturn(List.of(createAssignment(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR)));
        when(plantationAssignmentRepository.existsByPlantationIdAndPersonnelIdAndRole(kebunId, supirUuid, PlantationAssignment.PersonnelRole.SUPIR))
                .thenReturn(false);

        CreatePengirimanRequest request = new CreatePengirimanRequest(supirId, List.of(harvestId1.toString()));
        assertThrows(BadRequestException.class, () -> pengirimanService.createPengiriman(mandorId, request));
    }

    @Test
    void createPengiriman_mandorNotOnKebun_throwsBadRequest() {
        when(plantationAssignmentRepository.findByPersonnelIdAndRole(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR))
                .thenReturn(List.of());

        CreatePengirimanRequest request = new CreatePengirimanRequest(supirId, List.of(harvestId1.toString()));
        assertThrows(BadRequestException.class, () -> pengirimanService.createPengiriman(mandorId, request));
    }

    @Test
    void pengirimanAktifSupir_returnsActiveDeliveries() {
        when(pengirimanRepository.findBySupirIdAndStatusIn(eq(supirId), anyList())).thenReturn(List.of(pengiriman));
        var result = pengirimanService.pengirimanAktifSupir(supirId);
        assertEquals(1, result.size());
    }

    @Test
    void riwayatSupir_returnsDeliveriesInDateRange() {
        when(pengirimanRepository.findBySupirIdAndCreatedAtBetween(eq(supirId), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(pengiriman));

        List<Pengiriman> result = pengirimanService.riwayatSupir(
                supirId, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertEquals(1, result.size());
        verify(pengirimanRepository).findBySupirIdAndCreatedAtBetween(
                eq(supirId), any(Instant.class), any(Instant.class));
    }

    @Test
    void pengirimanAktifMandor_returnsActiveDeliveries() {
        when(pengirimanRepository.findByMandorIdAndStatusIn(eq(mandorId), anyList())).thenReturn(List.of(pengiriman));

        List<Pengiriman> result = pengirimanService.pengirimanAktifMandor(mandorId);

        assertEquals(1, result.size());
    }

    @Test
    void pengirimanBySupirForMandor_validatesAssignmentAndReturnsDeliveries() {
        when(plantationAssignmentRepository.findByPersonnelIdAndRole(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR))
                .thenReturn(List.of(createAssignment(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR)));
        when(plantationAssignmentRepository.existsByPlantationIdAndPersonnelIdAndRole(
                kebunId, supirUuid, PlantationAssignment.PersonnelRole.SUPIR)).thenReturn(true);
        when(pengirimanRepository.findBySupirIdAndMandorIdAndCreatedAtBetween(
                eq(supirId), eq(mandorId), any(Instant.class), any(Instant.class))).thenReturn(List.of(pengiriman));

        List<Pengiriman> result = pengirimanService.pengirimanBySupirForMandor(
                mandorId, supirId, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertEquals(1, result.size());
    }

    @Test
    void updateStatusSupir_memuatToMengirim_success() {
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));
        when(statusTransitionPolicy.canTransition(PengirimanStatus.MEMUAT, PengirimanStatus.MENGIRIM)).thenReturn(true);

        Pengiriman result = pengirimanService.updateStatusSupir(supirId, pengirimanId, PengirimanStatus.MENGIRIM);

        assertEquals(PengirimanStatus.MENGIRIM, result.getStatus());
    }

    @Test
    void updateStatusSupir_mengirimToTibaPublishesEventAndMovesToMandorReview() {
        pengiriman.setStatus(PengirimanStatus.MENGIRIM);
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));
        when(statusTransitionPolicy.canTransition(PengirimanStatus.MENGIRIM, PengirimanStatus.TIBA_DI_TUJUAN))
                .thenReturn(true);

        Pengiriman result = pengirimanService.updateStatusSupir(
                supirId, pengirimanId, PengirimanStatus.TIBA_DI_TUJUAN);

        assertEquals(PengirimanStatus.PENDING_MANDOR_REVIEW, result.getStatus());
        verify(eventPublisher).publishPengirimanTiba(pengiriman);
    }

    @Test
    void updateStatusSupir_missingDelivery_throwsBadRequest() {
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class,
                () -> pengirimanService.updateStatusSupir(supirId, pengirimanId, PengirimanStatus.MENGIRIM));
    }

    @Test
    void updateStatusSupir_foreignSupir_throwsForbidden() {
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));

        assertThrows(ForbiddenException.class,
                () -> pengirimanService.updateStatusSupir(UUID.randomUUID().toString(), pengirimanId, PengirimanStatus.MENGIRIM));
    }

    @Test
    void updateStatusSupir_invalidTransition_throwsBadRequest() {
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));
        when(statusTransitionPolicy.canTransition(PengirimanStatus.MEMUAT, PengirimanStatus.TIBA_DI_TUJUAN)).thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> pengirimanService.updateStatusSupir(supirId, pengirimanId, PengirimanStatus.TIBA_DI_TUJUAN));
    }

    @Test
    void approveByMandor_successPublishesEvent() {
        pengiriman.setStatus(PengirimanStatus.PENDING_MANDOR_REVIEW);
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));

        Pengiriman result = pengirimanService.approveByMandor(mandorId, pengirimanId);

        assertEquals(PengirimanStatus.PENDING_ADMIN_REVIEW, result.getStatus());
        verify(eventPublisher).publishPengirimanApprovedMandor(pengiriman);
    }

    @Test
    void approveByMandor_wrongStatus_throwsBadRequest() {
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));

        assertThrows(BadRequestException.class, () -> pengirimanService.approveByMandor(mandorId, pengirimanId));
    }

    @Test
    void approveByMandor_foreignMandor_throwsForbidden() {
        pengiriman.setStatus(PengirimanStatus.PENDING_MANDOR_REVIEW);
        pengiriman.setMandorId(UUID.randomUUID().toString());
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));

        assertThrows(ForbiddenException.class, () -> pengirimanService.approveByMandor(mandorId, pengirimanId));
    }

    @Test
    void rejectByMandor_successTrimsReason() {
        pengiriman.setStatus(PengirimanStatus.PENDING_MANDOR_REVIEW);
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));

        Pengiriman result = pengirimanService.rejectByMandor(mandorId, pengirimanId, " rusak ");

        assertEquals(PengirimanStatus.REJECTED_MANDOR, result.getStatus());
        assertEquals("rusak", result.getRejectedReason());
    }

    @Test
    void rejectByMandor_requiresReason() {
        assertThrows(BadRequestException.class, () -> pengirimanService.rejectByMandor(mandorId, pengirimanId, " "));
    }

    @Test
    void pendingAdmin_filtersByEmptySearchAndDateCombinations() {
        LocalDate date = LocalDate.of(2026, 5, 21);
        when(pengirimanRepository.findByStatus(PengirimanStatus.PENDING_ADMIN_REVIEW)).thenReturn(List.of(pengiriman));
        when(pengirimanRepository.findByStatusAndMandorIdContainingIgnoreCase(PengirimanStatus.PENDING_ADMIN_REVIEW, mandorId))
                .thenReturn(List.of(pengiriman));
        when(pengirimanRepository.findByStatusAndCreatedAtBetween(
                eq(PengirimanStatus.PENDING_ADMIN_REVIEW), any(Instant.class), any(Instant.class))).thenReturn(List.of(pengiriman));
        when(pengirimanRepository.findByStatusAndMandorIdContainingIgnoreCaseAndCreatedAtBetween(
                eq(PengirimanStatus.PENDING_ADMIN_REVIEW), eq(mandorId), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(pengiriman));

        assertEquals(1, pengirimanService.pendingAdmin(null, null).size());
        assertEquals(1, pengirimanService.pendingAdmin(" " + mandorId + " ", null).size());
        assertEquals(1, pengirimanService.pendingAdmin(null, date).size());
        assertEquals(1, pengirimanService.pendingAdmin(mandorId, date).size());
    }

    @Test
    void getById_missing_throwsBadRequest() {
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> pengirimanService.getById(pengirimanId));
    }

    @Test
    void approveByAdmin_successPublishesEvent() {
        pengiriman.setStatus(PengirimanStatus.PENDING_ADMIN_REVIEW);
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));

        Pengiriman result = pengirimanService.approveByAdmin(pengirimanId);

        assertEquals(PengirimanStatus.APPROVED_ADMIN, result.getStatus());
        verify(eventPublisher).publishPengirimanApprovedAdmin(pengiriman, pengiriman.getTotalKg());
    }

    @Test
    void approveByAdmin_wrongStatus_throwsBadRequest() {
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));

        assertThrows(BadRequestException.class, () -> pengirimanService.approveByAdmin(pengirimanId));
    }

    @Test
    void partialRejectByAdmin_successPublishesRecognizedKg() {
        pengiriman.setStatus(PengirimanStatus.PENDING_ADMIN_REVIEW);
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));

        Pengiriman result = pengirimanService.partialRejectByAdmin(pengirimanId, 80, " kurang ");

        assertEquals(PengirimanStatus.PARTIAL_REJECTED_ADMIN, result.getStatus());
        assertEquals(80, result.getRecognizedKg());
        assertEquals("kurang", result.getRejectedReason());
        verify(eventPublisher).publishPengirimanApprovedAdmin(pengiriman, 80);
    }

    @Test
    void partialRejectByAdmin_rejectsBlankReasonAndInvalidKg() {
        assertThrows(BadRequestException.class,
                () -> pengirimanService.partialRejectByAdmin(pengirimanId, 80, " "));

        pengiriman.setStatus(PengirimanStatus.PENDING_ADMIN_REVIEW);
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));
        assertThrows(BadRequestException.class,
                () -> pengirimanService.partialRejectByAdmin(pengirimanId, 0, "kurang"));
        assertThrows(BadRequestException.class,
                () -> pengirimanService.partialRejectByAdmin(pengirimanId, pengiriman.getTotalKg() + 1, "kurang"));
    }

    @Test
    void rejectByAdmin_successTrimsReason() {
        pengiriman.setStatus(PengirimanStatus.PENDING_ADMIN_REVIEW);
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));

        Pengiriman result = pengirimanService.rejectByAdmin(pengirimanId, " tidak layak ");

        assertEquals(PengirimanStatus.REJECTED_ADMIN, result.getStatus());
        assertEquals("tidak layak", result.getRejectedReason());
    }

    @Test
    void rejectByAdmin_requiresReason() {
        assertThrows(BadRequestException.class, () -> pengirimanService.rejectByAdmin(pengirimanId, " "));
    }

    @Test
    void listSupirOnKebunMandor_devProfileSyncsAuthDrivers() {
        UUID devDriverId = UUID.randomUUID();
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true);
        when(plantationAssignmentRepository.findByPersonnelIdAndRole(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR))
                .thenReturn(List.of(createAssignment(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR)));
        when(plantationAssignmentRepository.findByPlantationIdAndRole(kebunId, PlantationAssignment.PersonnelRole.SUPIR))
                .thenReturn(List.of());
        when(driverDirectoryLookup.fetchUsersByRole("DRIVER"))
                .thenReturn(List.of(new AuthUserClient.UserSummary(devDriverId, "Dev Supir", "dev@example.test")));
        when(plantationAssignmentRepository.findByPlantationIdAndPersonnelIdAndRole(
                kebunId, devDriverId, PlantationAssignment.PersonnelRole.SUPIR)).thenReturn(Optional.empty());
        when(plantationAssignmentRepository.save(any(PlantationAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(driverProfileLookup.fetchUsersByIds(List.of(devDriverId)))
                .thenReturn(Map.of(devDriverId, new AuthUserClient.UserSummary(devDriverId, "Dev Supir", "dev@example.test")));

        List<Map<String, Object>> result = pengirimanService.listSupirOnKebunMandor(mandorId, "");

        assertEquals(1, result.size());
        assertEquals("Dev Supir", result.getFirst().get("nama"));
    }

    @Test
    void createPengiriman_devProfileAutoAssignsSupir() {
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true);
        when(plantationAssignmentRepository.findByPersonnelIdAndRole(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR))
                .thenReturn(List.of(createAssignment(mandorUuid, PlantationAssignment.PersonnelRole.MANDOR)));
        when(plantationAssignmentRepository.existsByPlantationIdAndPersonnelIdAndRole(
                kebunId, supirUuid, PlantationAssignment.PersonnelRole.SUPIR)).thenReturn(false);
        when(plantationAssignmentRepository.findByPlantationIdAndPersonnelIdAndRole(
                kebunId, supirUuid, PlantationAssignment.PersonnelRole.SUPIR)).thenReturn(Optional.empty());
        when(plantationAssignmentRepository.save(any(PlantationAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(harvestResultRepository.findAllById(anyList())).thenReturn(List.of(harvest1));
        when(pengirimanRepository.save(any(Pengiriman.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pengiriman result = pengirimanService.createPengiriman(
                mandorId, new CreatePengirimanRequest(supirId, List.of(harvestId1.toString())));

        assertEquals(PengirimanStatus.MEMUAT, result.getStatus());
        verify(plantationAssignmentRepository).save(any(PlantationAssignment.class));
    }

}
