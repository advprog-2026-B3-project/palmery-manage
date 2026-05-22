package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.dto.CreatePengirimanRequest;
import id.ac.ui.cs.advprog.palmerymanage.exception.BadRequestException;
import id.ac.ui.cs.advprog.palmerymanage.exception.ForbiddenException;
import id.ac.ui.cs.advprog.palmerymanage.exception.OverWeightException;
import id.ac.ui.cs.advprog.palmerymanage.model.AdminApprovalStatus;
import id.ac.ui.cs.advprog.palmerymanage.model.ApprovalStatus;
import id.ac.ui.cs.advprog.palmerymanage.model.HarvestResult;
import id.ac.ui.cs.advprog.palmerymanage.model.Pengiriman;
import id.ac.ui.cs.advprog.palmerymanage.model.PengirimanStatus;
import id.ac.ui.cs.advprog.palmerymanage.model.Plantation;
import id.ac.ui.cs.advprog.palmerymanage.model.PlantationAssignment;
import id.ac.ui.cs.advprog.palmerymanage.model.PlantationAssignment.PersonnelRole;
import id.ac.ui.cs.advprog.palmerymanage.pengiriman.DriverDirectoryLookup;
import id.ac.ui.cs.advprog.palmerymanage.pengiriman.DriverProfileLookup;
import id.ac.ui.cs.advprog.palmerymanage.pengiriman.PengirimanEventPublisher;
import id.ac.ui.cs.advprog.palmerymanage.pengiriman.PengirimanStatusTransitionPolicy;
import id.ac.ui.cs.advprog.palmerymanage.repository.HarvestResultRepository;
import id.ac.ui.cs.advprog.palmerymanage.repository.PengirimanRepository;
import id.ac.ui.cs.advprog.palmerymanage.repository.PlantationAssignmentRepository;
import id.ac.ui.cs.advprog.palmerymanage.repository.PlantationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class PengirimanService {

    static final int MAX_TOTAL_KG = 400;

    private static final List<PengirimanStatus> DELIVERY_ACTIVE = List.of(
            PengirimanStatus.MEMUAT,
            PengirimanStatus.MENGIRIM,
            PengirimanStatus.TIBA_DI_TUJUAN
    );

    private final PengirimanRepository pengirimanRepository;
    private final HarvestResultRepository harvestResultRepository;
    private final PlantationAssignmentRepository plantationAssignmentRepository;
    private final PlantationRepository plantationRepository;
    private final PengirimanEventPublisher eventPublisher;
    private final PengirimanStatusTransitionPolicy statusTransitionPolicy;
    private final DriverProfileLookup driverProfileLookup;
    private final DriverDirectoryLookup driverDirectoryLookup;
    private final Environment environment;

    public PengirimanService(PengirimanRepository pengirimanRepository,
                             HarvestResultRepository harvestResultRepository,
                             PlantationAssignmentRepository plantationAssignmentRepository,
                             PlantationRepository plantationRepository,
                             PengirimanEventPublisher eventPublisher,
                             PengirimanStatusTransitionPolicy statusTransitionPolicy,
                             DriverProfileLookup driverProfileLookup,
                             DriverDirectoryLookup driverDirectoryLookup,
                             Environment environment) {
        this.pengirimanRepository = pengirimanRepository;
        this.harvestResultRepository = harvestResultRepository;
        this.plantationAssignmentRepository = plantationAssignmentRepository;
        this.plantationRepository = plantationRepository;
        this.eventPublisher = eventPublisher;
        this.statusTransitionPolicy = statusTransitionPolicy;
        this.driverProfileLookup = driverProfileLookup;
        this.driverDirectoryLookup = driverDirectoryLookup;
        this.environment = environment;
    }

    // ─── Supir list for Mandor ────────────────────────────────────────────────

    public List<Map<String, Object>> listSupirOnKebunMandor(String mandorId, String search) {
        UUID mandorUuid = parsePersonnelId(mandorId, "Mandor");
        UUID kebunId = resolveKebunForMandor(mandorUuid);

        List<PlantationAssignment> supirAssignments =
                plantationAssignmentRepository.findByPlantationIdAndRole(kebunId, PersonnelRole.SUPIR);

        if (isDevProfile()) {
            List<AuthUserClient.UserSummary> authDrivers = driverDirectoryLookup.fetchUsersByRole("DRIVER");
            if (!authDrivers.isEmpty()) {
                supirAssignments = authDrivers.stream()
                        .map(driver -> assignPersonnelIfMissing(kebunId, driver.id(), PersonnelRole.SUPIR))
                        .toList();
            }
        }

        List<UUID> supirIds = supirAssignments.stream()
                .map(PlantationAssignment::getPersonnelId)
                .toList();
        Map<UUID, AuthUserClient.UserSummary> profiles = driverProfileLookup.fetchUsersByIds(supirIds);

        String searchLower = search == null ? "" : search.trim().toLowerCase();
        List<Map<String, Object>> result = new ArrayList<>();
        for (PlantationAssignment assignment : supirAssignments) {
            UUID personnelId = assignment.getPersonnelId();
            String supirId = personnelId.toString();
            AuthUserClient.UserSummary profile = profiles.get(personnelId);
            String displayName = profile != null
                    ? profile.nama()
                    : "Supir " + supirId.substring(0, Math.min(8, supirId.length()));
            String kontak = profile != null ? profile.email() : "";

            if (!searchLower.isEmpty()
                    && !displayName.toLowerCase().contains(searchLower)
                    && !supirId.toLowerCase().contains(searchLower)
                    && !kontak.toLowerCase().contains(searchLower)) {
                continue;
            }
            result.add(Map.of(
                    "id", supirId,
                    "nama", displayName,
                    "kebun_id", kebunId.toString(),
                    "kontak", kontak
            ));
        }
        return result;
    }

    // ─── Create Pengiriman (Mandor) ───────────────────────────────────────────

    public Pengiriman createPengiriman(String mandorId, CreatePengirimanRequest request) {
        if (mandorId == null || mandorId.isBlank()) {
            throw new BadRequestException("Mandor tidak ditemukan");
        }
        if (request.supirId() == null || request.supirId().isBlank()) {
            throw new BadRequestException("Supir harus dipilih");
        }

        UUID mandorUuid = parsePersonnelId(mandorId, "Mandor");
        UUID kebunId = resolveKebunForMandor(mandorUuid);
        validateSupirOnKebun(request.supirId(), kebunId);

        List<UUID> panenUuids = request.panenIds().stream().map(id -> {
            try {
                return UUID.fromString(id);
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Format panen_id tidak valid: " + id);
            }
        }).toList();

        List<HarvestResult> panenList = harvestResultRepository.findAllById(panenUuids);
        if (panenList.size() != panenUuids.size()) {
            throw new BadRequestException("Beberapa hasil panen tidak ditemukan");
        }

        // Only APPROVED harvests can be shipped
        boolean hasNonApproved = panenList.stream()
                .anyMatch(panen -> !"APPROVED".equals(panen.getStatus()));
        if (hasNonApproved) {
            throw new BadRequestException("Semua hasil panen harus berstatus APPROVED");
        }

        boolean hasNonReady = panenList.stream()
                .anyMatch(panen -> !Boolean.TRUE.equals(panen.getReadyForDelivery()));
        if (hasNonReady) {
            throw new BadRequestException("Semua hasil panen harus berstatus Siap Angkut");
        }

        int total = (int) Math.round(panenList.stream()
                .mapToDouble(p -> p.getKgHarvested() == null ? 0.0 : p.getKgHarvested())
                .sum());
        if (total > MAX_TOTAL_KG) {
            throw new OverWeightException("Total berat maksimum 400 kg");
        }

        boolean notOwned = panenList.stream()
                .anyMatch(p -> p.getMandorId() == null || !p.getMandorId().equals(mandorUuid));
        if (notOwned) {
            throw new ForbiddenException("Panen tidak berada di bawah mandor ini");
        }

        boolean wrongKebun = panenList.stream()
                .anyMatch(p -> p.getPlantationId() == null || !p.getPlantationId().equals(kebunId));
        if (wrongKebun) {
            throw new BadRequestException("Hasil panen harus berasal dari kebun yang sama dengan mandor");
        }

        // Mark harvests as no longer available for delivery
        for (HarvestResult panen : panenList) {
            panen.setReadyForDelivery(false);
        }
        harvestResultRepository.saveAll(panenList);

        Pengiriman pengiriman = new Pengiriman();
        pengiriman.setSupirId(request.supirId());
        pengiriman.setMandorId(mandorId);
        pengiriman.setKebunId(kebunId.toString());
        pengiriman.setTotalKg(total);
        pengiriman.setPanenIds(request.panenIds());
        pengiriman.setStatus(PengirimanStatus.MEMUAT);
        pengiriman.setMandorApprovalStatus(ApprovalStatus.PENDING);
        pengiriman.setAdminApprovalStatus(AdminApprovalStatus.PENDING);

        log.info("Pengiriman created: supirId={}, mandorId={}, totalKg={}, panenCount={}",
                request.supirId(), mandorId, total, panenList.size());
        return pengirimanRepository.save(pengiriman);
    }

    // ─── Supir: update delivery status ────────────────────────────────────────

    public Pengiriman updateStatusSupir(String supirId, UUID id, PengirimanStatus target) {
        Pengiriman pengiriman = pengirimanRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Pengiriman tidak ditemukan"));

        if (!pengiriman.getSupirId().equals(supirId)) {
            throw new ForbiddenException("Supir tidak berhak mengubah pengiriman ini");
        }

        PengirimanStatus current = pengiriman.getStatus();
        if (!statusTransitionPolicy.canTransition(current, target)) {
            throw new BadRequestException("Transisi status tidak valid: " + current + " -> " + target);
        }

        pengiriman.setStatus(target);

        if (target == PengirimanStatus.TIBA_DI_TUJUAN) {
            eventPublisher.publishPengirimanTiba(pengiriman);
            pengiriman.setStatus(PengirimanStatus.PENDING_MANDOR_REVIEW);
        }

        return pengirimanRepository.save(pengiriman);
    }

    // ─── Mandor: approve/reject after TIBA_DI_TUJUAN ──────────────────────────

    public Pengiriman approveByMandor(String mandorId, UUID id) {
        Pengiriman pengiriman = requireOwnedByMandor(mandorId, id);
        if (pengiriman.getStatus() != PengirimanStatus.PENDING_MANDOR_REVIEW) {
            throw new BadRequestException("Pengiriman belum tiba di tujuan");
        }
        if (pengiriman.getMandorApprovalStatus() != ApprovalStatus.PENDING) {
            throw new BadRequestException("Pengiriman sudah diproses mandor");
        }

        pengiriman.setStatus(PengirimanStatus.PENDING_ADMIN_REVIEW);
        pengiriman.setMandorApprovalStatus(ApprovalStatus.APPROVED);
        eventPublisher.publishPengirimanApprovedMandor(pengiriman);

        log.info("Pengiriman {} approved by mandor {}", id, mandorId);
        return pengirimanRepository.save(pengiriman);
    }

    public Pengiriman rejectByMandor(String mandorId, UUID id, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BadRequestException("Alasan penolakan wajib diisi");
        }
        Pengiriman pengiriman = requireOwnedByMandor(mandorId, id);
        if (pengiriman.getStatus() != PengirimanStatus.PENDING_MANDOR_REVIEW) {
            throw new BadRequestException("Pengiriman belum tiba di tujuan");
        }
        if (pengiriman.getMandorApprovalStatus() != ApprovalStatus.PENDING) {
            throw new BadRequestException("Pengiriman sudah diproses mandor");
        }

        pengiriman.setStatus(PengirimanStatus.REJECTED_MANDOR);
        pengiriman.setMandorApprovalStatus(ApprovalStatus.REJECTED);
        pengiriman.setRejectedReason(reason.trim());

        log.info("Pengiriman {} rejected by mandor {}: {}", id, mandorId, reason);
        return pengirimanRepository.save(pengiriman);
    }

    // ─── Admin: validate after Mandor approves ────────────────────────────────

    public List<Pengiriman> pendingAdmin(String mandorSearch, LocalDate date) {
        String search = mandorSearch == null ? "" : mandorSearch.trim();
        boolean hasSearch = !search.isBlank();
        if (date == null && !hasSearch) {
            return pengirimanRepository.findByStatus(PengirimanStatus.PENDING_ADMIN_REVIEW);
        }
        if (date == null) {
            return pengirimanRepository.findByStatusAndMandorIdContainingIgnoreCase(
                    PengirimanStatus.PENDING_ADMIN_REVIEW, search);
        }

        Instant start = date.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        if (!hasSearch) {
            return pengirimanRepository.findByStatusAndCreatedAtBetween(
                    PengirimanStatus.PENDING_ADMIN_REVIEW, start, end);
        }
        return pengirimanRepository.findByStatusAndMandorIdContainingIgnoreCaseAndCreatedAtBetween(
                PengirimanStatus.PENDING_ADMIN_REVIEW, search, start, end);
    }

    public Pengiriman approveByAdmin(UUID id) {
        Pengiriman pengiriman = requirePendingAdminReview(id);

        pengiriman.setStatus(PengirimanStatus.APPROVED_ADMIN);
        pengiriman.setAdminApprovalStatus(AdminApprovalStatus.APPROVED);
        pengiriman.setAcceptedKgByAdmin(pengiriman.getTotalKg());
        eventPublisher.publishPengirimanApprovedAdmin(pengiriman, pengiriman.getTotalKg());

        log.info("Pengiriman {} fully approved by admin, acceptedKg={}", id, pengiriman.getTotalKg());
        return pengirimanRepository.save(pengiriman);
    }

    public Pengiriman partialRejectByAdmin(UUID id, int acceptedKg, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BadRequestException("Alasan penolakan wajib diisi");
        }
        Pengiriman pengiriman = requirePendingAdminReview(id);

        if (acceptedKg <= 0 || acceptedKg >= pengiriman.getTotalKg()) {
            throw new BadRequestException("Berat yang diakui harus > 0 dan < total (" + pengiriman.getTotalKg() + " kg)");
        }

        pengiriman.setStatus(PengirimanStatus.PARTIAL_REJECTED_ADMIN);
        pengiriman.setAdminApprovalStatus(AdminApprovalStatus.PARTIALLY_APPROVED);
        pengiriman.setAcceptedKgByAdmin(acceptedKg);
        pengiriman.setRecognizedKg(acceptedKg);
        pengiriman.setRejectedReason(reason.trim());
        eventPublisher.publishPengirimanApprovedAdmin(pengiriman, acceptedKg);

        log.info("Pengiriman {} partially approved by admin, acceptedKg={}/{}", id, acceptedKg, pengiriman.getTotalKg());
        return pengirimanRepository.save(pengiriman);
    }

    public Pengiriman rejectByAdmin(UUID id, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BadRequestException("Alasan penolakan wajib diisi");
        }
        Pengiriman pengiriman = requirePendingAdminReview(id);

        pengiriman.setStatus(PengirimanStatus.REJECTED_ADMIN);
        pengiriman.setAdminApprovalStatus(AdminApprovalStatus.REJECTED);
        pengiriman.setRejectedReason(reason.trim());

        log.info("Pengiriman {} rejected by admin: {}", id, reason);
        return pengirimanRepository.save(pengiriman);
    }

    // ─── Queries ──────────────────────────────────────────────────────────────

    public Pengiriman getById(UUID id) {
        return pengirimanRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Pengiriman tidak ditemukan"));
    }

    public List<Pengiriman> pengirimanAktifSupir(String supirId) {
        return pengirimanRepository.findBySupirIdAndStatusIn(supirId, DELIVERY_ACTIVE);
    }

    public List<Pengiriman> riwayatSupir(String supirId, LocalDate from, LocalDate to) {
        Instant fromInstant = from.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant toInstant = to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        return pengirimanRepository.findBySupirIdAndCreatedAtBetween(supirId, fromInstant, toInstant);
    }

    public List<Pengiriman> pengirimanAktifMandor(String mandorId) {
        return pengirimanRepository.findByMandorIdAndStatusIn(mandorId, DELIVERY_ACTIVE);
    }

    public List<Pengiriman> pengirimanBySupirForMandor(
            String mandorId, String supirId, LocalDate from, LocalDate to) {
        requireSupirOnMandorKebun(mandorId, supirId);
        Instant fromInstant = from.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant toInstant = to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        return pengirimanRepository.findBySupirIdAndMandorIdAndCreatedAtBetween(
                supirId, mandorId, fromInstant, toInstant);
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private Pengiriman requireOwnedByMandor(String mandorId, UUID id) {
        Pengiriman pengiriman = getById(id);
        if (!pengiriman.getMandorId().equals(mandorId)) {
            throw new ForbiddenException("Mandor tidak berhak mengubah pengiriman ini");
        }
        return pengiriman;
    }

    private Pengiriman requirePendingAdminReview(UUID id) {
        Pengiriman pengiriman = getById(id);
        if (pengiriman.getStatus() != PengirimanStatus.PENDING_ADMIN_REVIEW) {
            throw new BadRequestException("Pengiriman belum disetujui mandor");
        }
        if (pengiriman.getAdminApprovalStatus() != AdminApprovalStatus.PENDING) {
            throw new BadRequestException("Pengiriman sudah diproses admin");
        }
        return pengiriman;
    }

    private void requireSupirOnMandorKebun(String mandorId, String supirId) {
        UUID mandorUuid = parsePersonnelId(mandorId, "Mandor");
        UUID kebunId = resolveKebunForMandor(mandorUuid);
        validateSupirOnKebun(supirId, kebunId);
    }

    private UUID resolveKebunForMandor(UUID mandorUuid) {
        List<PlantationAssignment> assignments = plantationAssignmentRepository
                .findByPersonnelIdAndRole(mandorUuid, PersonnelRole.MANDOR);
        if (assignments.isEmpty()) {
            if (isDevProfile()) {
                UUID kebunId = ensureDevPlantation().getId();
                return assignPersonnelIfMissing(kebunId, mandorUuid, PersonnelRole.MANDOR).getPlantationId();
            }
            throw new BadRequestException("Mandor belum ditugaskan ke kebun");
        }
        return assignments.getFirst().getPlantationId();
    }

    private void validateSupirOnKebun(String supirId, UUID kebunId) {
        UUID supirUuid = parsePersonnelId(supirId, "Supir");
        boolean assigned = plantationAssignmentRepository
                .existsByPlantationIdAndPersonnelIdAndRole(kebunId, supirUuid, PersonnelRole.SUPIR);
        if (!assigned) {
            if (isDevProfile()) {
                assignPersonnelIfMissing(kebunId, supirUuid, PersonnelRole.SUPIR);
                return;
            }
            throw new BadRequestException("Supir tidak bertugas di kebun yang sama dengan mandor");
        }
    }

    private Plantation ensureDevPlantation() {
        return plantationRepository.findByCode("DEV-KEBUN-1")
                .orElseGet(() -> plantationRepository.save(Plantation.builder()
                        .name("Kebun Dev")
                        .code("DEV-KEBUN-1")
                        .areaHa(1.0)
                        .coordTlLat(0.0)
                        .coordTlLon(0.0)
                        .coordTrLat(0.0)
                        .coordTrLon(0.01)
                        .coordBrLat(-0.01)
                        .coordBrLon(0.01)
                        .coordBlLat(-0.01)
                        .coordBlLon(0.0)
                        .isActive(true)
                        .build()));
    }

    private PlantationAssignment assignPersonnelIfMissing(UUID kebunId, UUID personnelId, PersonnelRole role) {
        return plantationAssignmentRepository
                .findByPlantationIdAndPersonnelIdAndRole(kebunId, personnelId, role)
                .orElseGet(() -> plantationAssignmentRepository.save(PlantationAssignment.builder()
                        .plantationId(kebunId)
                        .personnelId(personnelId)
                        .role(role)
                        .build()));
    }

    private boolean isDevProfile() {
        return environment.acceptsProfiles(Profiles.of("dev"));
    }

    private UUID parsePersonnelId(String rawId, String label) {
        try {
            return UUID.fromString(rawId);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(label + " ID tidak valid: " + rawId);
        }
    }
}
