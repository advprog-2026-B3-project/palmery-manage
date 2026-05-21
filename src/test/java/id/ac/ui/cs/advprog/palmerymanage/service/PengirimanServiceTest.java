package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.dto.CreatePengirimanRequest;
import id.ac.ui.cs.advprog.palmerymanage.exception.BadRequestException;
import id.ac.ui.cs.advprog.palmerymanage.exception.ForbiddenException;
import id.ac.ui.cs.advprog.palmerymanage.exception.OverWeightException;
import id.ac.ui.cs.advprog.palmerymanage.model.HarvestResult;
import id.ac.ui.cs.advprog.palmerymanage.model.Pengiriman;
import id.ac.ui.cs.advprog.palmerymanage.model.PengirimanStatus;
import id.ac.ui.cs.advprog.palmerymanage.model.PlantationAssignment;
import id.ac.ui.cs.advprog.palmerymanage.model.PlantationAssignment.PersonnelRole;
import id.ac.ui.cs.advprog.palmerymanage.pengiriman.DriverDirectoryLookup;
import id.ac.ui.cs.advprog.palmerymanage.pengiriman.DriverPengirimanStatusTransitionPolicy;
import id.ac.ui.cs.advprog.palmerymanage.pengiriman.DriverProfileLookup;
import id.ac.ui.cs.advprog.palmerymanage.pengiriman.PengirimanEventPublisher;
import id.ac.ui.cs.advprog.palmerymanage.pengiriman.PengirimanStatusTransitionPolicy;
import id.ac.ui.cs.advprog.palmerymanage.repository.HarvestResultRepository;
import id.ac.ui.cs.advprog.palmerymanage.repository.PengirimanRepository;
import id.ac.ui.cs.advprog.palmerymanage.repository.PlantationAssignmentRepository;
import id.ac.ui.cs.advprog.palmerymanage.repository.PlantationRepository;
import id.ac.ui.cs.advprog.palmerymanage.service.AuthUserClient.UserSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

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

    @InjectMocks
    private PengirimanService pengirimanService;

    private UUID mandorId;
    private UUID supirId;
    private UUID kebunId;
    private UUID panenId;
    private UUID pengirimanId;

    @BeforeEach
    void setUp() {
        mandorId = UUID.randomUUID();
        supirId = UUID.randomUUID();
        kebunId = UUID.randomUUID();
        panenId = UUID.randomUUID();
        pengirimanId = UUID.randomUUID();

        lenient().when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(false);
    }

    private void stubMandorOnKebun() {
        PlantationAssignment mandorAssignment = PlantationAssignment.builder()
                .plantationId(kebunId)
                .personnelId(mandorId)
                .role(PersonnelRole.MANDOR)
                .build();
        when(plantationAssignmentRepository.findByPersonnelIdAndRole(mandorId, PersonnelRole.MANDOR))
                .thenReturn(List.of(mandorAssignment));
    }

    private void stubMandorAndSupirOnKebun() {
        stubMandorOnKebun();
        when(plantationAssignmentRepository.existsByPlantationIdAndPersonnelIdAndRole(
                kebunId, supirId, PersonnelRole.SUPIR)).thenReturn(true);
    }

    private HarvestResult readyPanen(int kg) {
        return HarvestResult.builder()
                .id(panenId)
                .mandorId(mandorId)
                .plantationId(kebunId)
                .kgHarvested((float) kg)
                .readyForDelivery(true)
                .status("APPROVED")
                .build();
    }

    private Pengiriman samplePengiriman(PengirimanStatus status) {
        Pengiriman p = new Pengiriman();
        p.setId(pengirimanId);
        p.setSupirId(supirId.toString());
        p.setMandorId(mandorId.toString());
        p.setKebunId(kebunId.toString());
        p.setTotalKg(100);
        p.setStatus(status);
        p.setPanenIds(List.of(panenId.toString()));
        return p;
    }

    @Test
    void listSupirOnKebunMandorReturnsDriverProfiles() {
        stubMandorOnKebun();
        PlantationAssignment supirAssignment = PlantationAssignment.builder()
                .plantationId(kebunId)
                .personnelId(supirId)
                .role(PersonnelRole.SUPIR)
                .build();
        when(plantationAssignmentRepository.findByPlantationIdAndRole(kebunId, PersonnelRole.SUPIR))
                .thenReturn(List.of(supirAssignment));
        when(driverProfileLookup.fetchUsersByIds(List.of(supirId)))
                .thenReturn(Map.of(supirId, new UserSummary(supirId, "Budi Supir", "budi@test.com")));

        List<Map<String, Object>> drivers = pengirimanService.listSupirOnKebunMandor(mandorId.toString(), "");

        assertEquals(1, drivers.size());
        assertEquals(supirId.toString(), drivers.getFirst().get("id"));
        assertEquals("Budi Supir", drivers.getFirst().get("nama"));
    }

    @Test
    void listSupirOnKebunMandorFiltersBySearch() {
        stubMandorOnKebun();
        UUID otherSupir = UUID.randomUUID();
        when(plantationAssignmentRepository.findByPlantationIdAndRole(kebunId, PersonnelRole.SUPIR))
                .thenReturn(List.of(
                        PlantationAssignment.builder().plantationId(kebunId).personnelId(supirId).role(PersonnelRole.SUPIR).build(),
                        PlantationAssignment.builder().plantationId(kebunId).personnelId(otherSupir).role(PersonnelRole.SUPIR).build()
                ));
        when(driverProfileLookup.fetchUsersByIds(anyList())).thenReturn(Map.of(
                supirId, new UserSummary(supirId, "Budi", "budi@test.com"),
                otherSupir, new UserSummary(otherSupir, "Andi", "andi@test.com")
        ));

        List<Map<String, Object>> drivers = pengirimanService.listSupirOnKebunMandor(mandorId.toString(), "budi");

        assertEquals(1, drivers.size());
        assertEquals("Budi", drivers.getFirst().get("nama"));
    }

    @Test
    void createPengirimanSuccess() {
        stubMandorAndSupirOnKebun();
        HarvestResult panen = readyPanen(100);
        when(harvestResultRepository.findAllById(anyList())).thenReturn(List.of(panen));
        when(pengirimanRepository.save(any(Pengiriman.class))).thenAnswer(inv -> {
            Pengiriman saved = inv.getArgument(0);
            saved.setId(pengirimanId);
            return saved;
        });

        CreatePengirimanRequest request = new CreatePengirimanRequest(supirId.toString(), List.of(panenId.toString()));
        Pengiriman result = pengirimanService.createPengiriman(mandorId.toString(), request);

        assertEquals(PengirimanStatus.MEMUAT, result.getStatus());
        assertEquals(100, result.getTotalKg());
        verify(harvestResultRepository).saveAll(anyList());
        verify(pengirimanRepository).save(any(Pengiriman.class));
    }

    @Test
    void createPengirimanRejectsBlankMandor() {
        assertThrows(BadRequestException.class,
                () -> pengirimanService.createPengiriman(" ", new CreatePengirimanRequest(supirId.toString(), List.of("x"))));
    }

    @Test
    void createPengirimanRejectsBlankSupir() {
        assertThrows(BadRequestException.class,
                () -> pengirimanService.createPengiriman(mandorId.toString(), new CreatePengirimanRequest(" ", List.of(panenId.toString()))));
    }

    @Test
    void createPengirimanRejectsInvalidPanenId() {
        stubMandorAndSupirOnKebun();
        assertThrows(BadRequestException.class,
                () -> pengirimanService.createPengiriman(mandorId.toString(),
                        new CreatePengirimanRequest(supirId.toString(), List.of("not-a-uuid"))));
    }

    @Test
    void createPengirimanRejectsMissingPanen() {
        stubMandorAndSupirOnKebun();
        when(harvestResultRepository.findAllById(anyList())).thenReturn(List.of());
        assertThrows(BadRequestException.class,
                () -> pengirimanService.createPengiriman(mandorId.toString(),
                        new CreatePengirimanRequest(supirId.toString(), List.of(panenId.toString()))));
    }

    @Test
    void createPengirimanRejectsPanenNotReady() {
        stubMandorAndSupirOnKebun();
        HarvestResult panen = readyPanen(50);
        panen.setReadyForDelivery(false);
        when(harvestResultRepository.findAllById(anyList())).thenReturn(List.of(panen));
        assertThrows(BadRequestException.class,
                () -> pengirimanService.createPengiriman(mandorId.toString(),
                        new CreatePengirimanRequest(supirId.toString(), List.of(panenId.toString()))));
    }

    @Test
    void createPengirimanRejectsOverWeight() {
        stubMandorAndSupirOnKebun();
        when(harvestResultRepository.findAllById(anyList())).thenReturn(List.of(readyPanen(500)));
        assertThrows(OverWeightException.class,
                () -> pengirimanService.createPengiriman(mandorId.toString(),
                        new CreatePengirimanRequest(supirId.toString(), List.of(panenId.toString()))));
    }

    @Test
    void createPengirimanRejectsPanenOwnedByOtherMandor() {
        stubMandorAndSupirOnKebun();
        HarvestResult panen = readyPanen(50);
        panen.setMandorId(UUID.randomUUID());
        when(harvestResultRepository.findAllById(anyList())).thenReturn(List.of(panen));
        assertThrows(ForbiddenException.class,
                () -> pengirimanService.createPengiriman(mandorId.toString(),
                        new CreatePengirimanRequest(supirId.toString(), List.of(panenId.toString()))));
    }

    @Test
    void createPengirimanRejectsPanenFromOtherKebun() {
        stubMandorAndSupirOnKebun();
        HarvestResult panen = readyPanen(50);
        panen.setPlantationId(UUID.randomUUID());
        when(harvestResultRepository.findAllById(anyList())).thenReturn(List.of(panen));
        assertThrows(BadRequestException.class,
                () -> pengirimanService.createPengiriman(mandorId.toString(),
                        new CreatePengirimanRequest(supirId.toString(), List.of(panenId.toString()))));
    }

    @Test
    void pengirimanAktifSupirDelegatesToRepository() {
        Pengiriman aktif = samplePengiriman(PengirimanStatus.MEMUAT);
        when(pengirimanRepository.findBySupirIdAndStatusIn(eq(supirId.toString()), anyList()))
                .thenReturn(List.of(aktif));

        List<Pengiriman> result = pengirimanService.pengirimanAktifSupir(supirId.toString());

        assertEquals(1, result.size());
    }

    @Test
    void riwayatSupirUsesDateRange() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);
        when(pengirimanRepository.findBySupirIdAndCreatedAtBetween(eq(supirId.toString()), any(), any()))
                .thenReturn(List.of());

        pengirimanService.riwayatSupir(supirId.toString(), from, to);

        verify(pengirimanRepository).findBySupirIdAndCreatedAtBetween(eq(supirId.toString()), any(Instant.class), any(Instant.class));
    }

    @Test
    void updateStatusSupirAdvancesToMengirim() {
        Pengiriman pengiriman = samplePengiriman(PengirimanStatus.MEMUAT);
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));
        when(statusTransitionPolicy.canTransition(PengirimanStatus.MEMUAT, PengirimanStatus.MENGIRIM)).thenReturn(true);

        Pengiriman updated = pengirimanService.updateStatusSupir(supirId.toString(), pengirimanId, PengirimanStatus.MENGIRIM);

        assertEquals(PengirimanStatus.MENGIRIM, updated.getStatus());
    }

    @Test
    void updateStatusSupirAtTibaPublishesEventAndSetsPendingMandorReview() {
        Pengiriman pengiriman = samplePengiriman(PengirimanStatus.MENGIRIM);
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));
        when(statusTransitionPolicy.canTransition(PengirimanStatus.MENGIRIM, PengirimanStatus.TIBA_DI_TUJUAN))
                .thenReturn(true);

        Pengiriman updated = pengirimanService.updateStatusSupir(
                supirId.toString(), pengirimanId, PengirimanStatus.TIBA_DI_TUJUAN);

        assertEquals(PengirimanStatus.PENDING_MANDOR_REVIEW, updated.getStatus());
        verify(eventPublisher).publishPengirimanTiba(pengiriman);
    }

    @Test
    void updateStatusSupirRejectsUnknownPengiriman() {
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.empty());
        assertThrows(BadRequestException.class,
                () -> pengirimanService.updateStatusSupir(supirId.toString(), pengirimanId, PengirimanStatus.MENGIRIM));
    }

    @Test
    void updateStatusSupirRejectsWrongSupir() {
        Pengiriman pengiriman = samplePengiriman(PengirimanStatus.MEMUAT);
        pengiriman.setSupirId(UUID.randomUUID().toString());
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));
        assertThrows(ForbiddenException.class,
                () -> pengirimanService.updateStatusSupir(supirId.toString(), pengirimanId, PengirimanStatus.MENGIRIM));
    }

    @Test
    void updateStatusSupirRejectsInvalidTransition() {
        Pengiriman pengiriman = samplePengiriman(PengirimanStatus.PENDING_MANDOR_REVIEW);
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));
        when(statusTransitionPolicy.canTransition(PengirimanStatus.PENDING_MANDOR_REVIEW, PengirimanStatus.MENGIRIM))
                .thenReturn(false);
        assertThrows(BadRequestException.class,
                () -> pengirimanService.updateStatusSupir(supirId.toString(), pengirimanId, PengirimanStatus.MENGIRIM));
    }

    @Test
    void approveByMandorMovesToPendingAdminReview() {
        Pengiriman pengiriman = samplePengiriman(PengirimanStatus.PENDING_MANDOR_REVIEW);
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));

        Pengiriman result = pengirimanService.approveByMandor(mandorId.toString(), pengirimanId);

        assertEquals(PengirimanStatus.PENDING_ADMIN_REVIEW, result.getStatus());
        verify(eventPublisher).publishPengirimanApprovedMandor(pengiriman);
    }

    @Test
    void approveByMandorRejectsWrongStatus() {
        Pengiriman pengiriman = samplePengiriman(PengirimanStatus.MEMUAT);
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));
        assertThrows(BadRequestException.class,
                () -> pengirimanService.approveByMandor(mandorId.toString(), pengirimanId));
    }

    @Test
    void rejectByMandorRequiresReason() {
        assertThrows(BadRequestException.class,
                () -> pengirimanService.rejectByMandor(mandorId.toString(), pengirimanId, " "));
    }

    @Test
    void rejectByMandorSetsRejectedStatus() {
        Pengiriman pengiriman = samplePengiriman(PengirimanStatus.PENDING_MANDOR_REVIEW);
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));

        Pengiriman result = pengirimanService.rejectByMandor(mandorId.toString(), pengirimanId, " rusak ");

        assertEquals(PengirimanStatus.REJECTED_MANDOR, result.getStatus());
        assertEquals("rusak", result.getRejectedReason());
    }

    @Test
    void pendingAdminWithSearchAndDate() {
        LocalDate date = LocalDate.of(2026, 5, 20);
        when(pengirimanRepository.findByStatusAndMandorIdContainingIgnoreCaseAndCreatedAtBetween(
                eq(PengirimanStatus.PENDING_ADMIN_REVIEW), eq("mdr"), any(), any()))
                .thenReturn(List.of());

        pengirimanService.pendingAdmin("mdr", date);

        verify(pengirimanRepository).findByStatusAndMandorIdContainingIgnoreCaseAndCreatedAtBetween(
                eq(PengirimanStatus.PENDING_ADMIN_REVIEW), eq("mdr"), any(), any());
    }

    @Test
    void pendingAdminWithSearchOnly() {
        when(pengirimanRepository.findByStatusAndMandorIdContainingIgnoreCase(
                PengirimanStatus.PENDING_ADMIN_REVIEW, "mdr")).thenReturn(List.of());
        pengirimanService.pendingAdmin("mdr", null);
        verify(pengirimanRepository).findByStatusAndMandorIdContainingIgnoreCase(
                PengirimanStatus.PENDING_ADMIN_REVIEW, "mdr");
    }

    @Test
    void pendingAdminWithDateOnly() {
        when(pengirimanRepository.findByStatusAndCreatedAtBetween(
                eq(PengirimanStatus.PENDING_ADMIN_REVIEW), any(), any())).thenReturn(List.of());
        pengirimanService.pendingAdmin(null, LocalDate.of(2026, 5, 20));
        verify(pengirimanRepository).findByStatusAndCreatedAtBetween(
                eq(PengirimanStatus.PENDING_ADMIN_REVIEW), any(), any());
    }

    @Test
    void pendingAdminWithoutFilters() {
        when(pengirimanRepository.findByStatus(PengirimanStatus.PENDING_ADMIN_REVIEW)).thenReturn(List.of());
        pengirimanService.pendingAdmin(null, null);
        verify(pengirimanRepository).findByStatus(PengirimanStatus.PENDING_ADMIN_REVIEW);
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.empty());
        assertThrows(BadRequestException.class, () -> pengirimanService.getById(pengirimanId));
    }

    @Test
    void approveByAdminPublishesEvent() {
        Pengiriman pengiriman = samplePengiriman(PengirimanStatus.PENDING_ADMIN_REVIEW);
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));

        Pengiriman result = pengirimanService.approveByAdmin(pengirimanId);

        assertEquals(PengirimanStatus.APPROVED_ADMIN, result.getStatus());
        verify(eventPublisher).publishPengirimanApprovedAdmin(pengiriman, pengiriman.getTotalKg());
    }

    @Test
    void partialRejectByAdminValidatesRecognizedKg() {
        Pengiriman pengiriman = samplePengiriman(PengirimanStatus.PENDING_ADMIN_REVIEW);
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));
        assertThrows(BadRequestException.class,
                () -> pengirimanService.partialRejectByAdmin(pengirimanId, 0, "alasan"));
        assertThrows(BadRequestException.class,
                () -> pengirimanService.partialRejectByAdmin(pengirimanId, 200, "alasan"));
    }

    @Test
    void partialRejectByAdminSuccess() {
        Pengiriman pengiriman = samplePengiriman(PengirimanStatus.PENDING_ADMIN_REVIEW);
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));

        Pengiriman result = pengirimanService.partialRejectByAdmin(pengirimanId, 80, " kurang ");

        assertEquals(PengirimanStatus.PARTIAL_REJECTED_ADMIN, result.getStatus());
        assertEquals(80, result.getRecognizedKg());
        assertEquals("kurang", result.getRejectedReason());
        verify(eventPublisher).publishPengirimanApprovedAdmin(pengiriman, 80);
    }

    @Test
    void rejectByAdminSetsRejectedStatus() {
        Pengiriman pengiriman = samplePengiriman(PengirimanStatus.PENDING_ADMIN_REVIEW);
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));

        Pengiriman result = pengirimanService.rejectByAdmin(pengirimanId, " tidak layak ");

        assertEquals(PengirimanStatus.REJECTED_ADMIN, result.getStatus());
        assertEquals("tidak layak", result.getRejectedReason());
    }

    @Test
    void rejectByMandorRejectsForeignMandor() {
        Pengiriman pengiriman = samplePengiriman(PengirimanStatus.PENDING_MANDOR_REVIEW);
        pengiriman.setMandorId(UUID.randomUUID().toString());
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));
        assertThrows(ForbiddenException.class,
                () -> pengirimanService.rejectByMandor(mandorId.toString(), pengirimanId, "alasan"));
    }

    @Test
    void pengirimanBySupirForMandorValidatesAssignment() {
        stubMandorAndSupirOnKebun();
        when(pengirimanRepository.findBySupirIdAndMandorIdAndCreatedAtBetween(
                anyString(), anyString(), any(), any())).thenReturn(List.of());

        pengirimanService.pengirimanBySupirForMandor(
                mandorId.toString(), supirId.toString(), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        verify(pengirimanRepository).findBySupirIdAndMandorIdAndCreatedAtBetween(
                eq(supirId.toString()), eq(mandorId.toString()), any(), any());
    }

    @Test
    void createPengirimanRejectsInvalidMandorId() {
        assertThrows(BadRequestException.class,
                () -> pengirimanService.createPengiriman("not-uuid",
                        new CreatePengirimanRequest(supirId.toString(), List.of(panenId.toString()))));
    }

    @Test
    void createPengirimanRejectsMandorWithoutKebunAssignment() {
        when(plantationAssignmentRepository.findByPersonnelIdAndRole(mandorId, PersonnelRole.MANDOR))
                .thenReturn(List.of());
        assertThrows(BadRequestException.class,
                () -> pengirimanService.createPengiriman(mandorId.toString(),
                        new CreatePengirimanRequest(supirId.toString(), List.of(panenId.toString()))));
    }

    @Test
    void pengirimanAktifMandorDelegatesToRepository() {
        when(pengirimanRepository.findByMandorIdAndStatusIn(eq(mandorId.toString()), anyList()))
                .thenReturn(List.of(samplePengiriman(PengirimanStatus.MEMUAT)));
        List<Pengiriman> result = pengirimanService.pengirimanAktifMandor(mandorId.toString());
        assertEquals(1, result.size());
    }

    @Test
    void rejectByAdminRequiresReason() {
        assertThrows(BadRequestException.class,
                () -> pengirimanService.rejectByAdmin(pengirimanId, " "));
    }

    @Test
    void approveByAdminRejectsWrongStatus() {
        Pengiriman pengiriman = samplePengiriman(PengirimanStatus.MEMUAT);
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));
        assertThrows(BadRequestException.class, () -> pengirimanService.approveByAdmin(pengirimanId));
    }

    @Test
    void partialRejectByAdminRequiresReason() {
        assertThrows(BadRequestException.class,
                () -> pengirimanService.partialRejectByAdmin(pengirimanId, 50, " "));
    }

    @Test
    void listSupirOnKebunMandorUsesAuthDriversInDevProfile() {
        lenient().when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true);
        stubMandorOnKebun();
        UUID driverId = UUID.randomUUID();
        when(driverDirectoryLookup.fetchUsersByRole("DRIVER"))
                .thenReturn(List.of(new UserSummary(driverId, "Dev Driver", "dev@test.com")));
        when(plantationAssignmentRepository.findByPlantationIdAndRole(kebunId, PersonnelRole.SUPIR))
                .thenReturn(List.of());
        when(plantationAssignmentRepository.findByPlantationIdAndPersonnelIdAndRole(
                kebunId, driverId, PersonnelRole.SUPIR)).thenReturn(Optional.empty());
        when(plantationAssignmentRepository.save(any(PlantationAssignment.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(driverProfileLookup.fetchUsersByIds(anyList()))
                .thenReturn(Map.of(driverId, new UserSummary(driverId, "Dev Driver", "dev@test.com")));

        List<Map<String, Object>> drivers = pengirimanService.listSupirOnKebunMandor(mandorId.toString(), "");

        assertEquals(1, drivers.size());
        assertEquals("Dev Driver", drivers.getFirst().get("nama"));
    }

    @Test
    void createPengirimanAutoAssignsSupirOnKebunInDevProfile() {
        lenient().when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true);
        stubMandorOnKebun();
        when(plantationAssignmentRepository.existsByPlantationIdAndPersonnelIdAndRole(
                kebunId, supirId, PersonnelRole.SUPIR)).thenReturn(false);
        when(plantationAssignmentRepository.findByPlantationIdAndPersonnelIdAndRole(
                kebunId, supirId, PersonnelRole.SUPIR)).thenReturn(Optional.empty());
        when(plantationAssignmentRepository.save(any(PlantationAssignment.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        HarvestResult panen = readyPanen(80);
        when(harvestResultRepository.findAllById(anyList())).thenReturn(List.of(panen));
        when(pengirimanRepository.save(any(Pengiriman.class))).thenAnswer(inv -> inv.getArgument(0));

        CreatePengirimanRequest request = new CreatePengirimanRequest(supirId.toString(), List.of(panenId.toString()));
        Pengiriman result = pengirimanService.createPengiriman(mandorId.toString(), request);

        assertEquals(PengirimanStatus.MEMUAT, result.getStatus());
        verify(plantationAssignmentRepository).save(any(PlantationAssignment.class));
    }

    @Test
    void createPengirimanRejectsSupirNotOnKebunWhenNotDev() {
        stubMandorOnKebun();
        when(plantationAssignmentRepository.existsByPlantationIdAndPersonnelIdAndRole(
                kebunId, supirId, PersonnelRole.SUPIR)).thenReturn(false);
        assertThrows(BadRequestException.class,
                () -> pengirimanService.createPengiriman(mandorId.toString(),
                        new CreatePengirimanRequest(supirId.toString(), List.of(panenId.toString()))));
    }

    @Test
    void usesDriverStatusTransitionPolicyBean() {
        PengirimanService serviceWithRealPolicy = new PengirimanService(
                pengirimanRepository,
                harvestResultRepository,
                plantationAssignmentRepository,
                plantationRepository,
                eventPublisher,
                new DriverPengirimanStatusTransitionPolicy(),
                driverProfileLookup,
                driverDirectoryLookup,
                environment
        );
        Pengiriman pengiriman = samplePengiriman(PengirimanStatus.MEMUAT);
        when(pengirimanRepository.findById(pengirimanId)).thenReturn(Optional.of(pengiriman));

        Pengiriman updated = serviceWithRealPolicy.updateStatusSupir(
                supirId.toString(), pengirimanId, PengirimanStatus.MENGIRIM);

        assertEquals(PengirimanStatus.MENGIRIM, updated.getStatus());
    }
}
