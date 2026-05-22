package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.dto.HarvestRequestDto;
import id.ac.ui.cs.advprog.palmerymanage.dto.ValidationRequestDto;
import id.ac.ui.cs.advprog.palmerymanage.event.HarvestApprovedEvent;
import id.ac.ui.cs.advprog.palmerymanage.event.HarvestEventPublisher;
import id.ac.ui.cs.advprog.palmerymanage.event.HarvestSubmittedEvent;
import id.ac.ui.cs.advprog.palmerymanage.model.HarvestPhoto;
import id.ac.ui.cs.advprog.palmerymanage.model.HarvestResult;
import id.ac.ui.cs.advprog.palmerymanage.model.Plantation;
import id.ac.ui.cs.advprog.palmerymanage.model.PlantationAssignment;
import id.ac.ui.cs.advprog.palmerymanage.model.WorkerAssignment;
import id.ac.ui.cs.advprog.palmerymanage.repository.HarvestResultRepository;
import id.ac.ui.cs.advprog.palmerymanage.repository.PlantationAssignmentRepository;
import id.ac.ui.cs.advprog.palmerymanage.repository.WorkerAssignmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import id.ac.ui.cs.advprog.palmerymanage.service.validation.HarvestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HarvestService {

    private static final Logger logger = LoggerFactory.getLogger(HarvestService.class);

    private final HarvestResultRepository harvestResultRepository;
    private final PlantationService plantationService;
    private final PlantationValidationService plantationValidationService;
    private final HarvestEventPublisher eventPublisher;
    private final List<HarvestValidator> validators;
    private final WorkerAssignmentRepository workerAssignmentRepository;
    private final PlantationAssignmentRepository plantationAssignmentRepository;

    @Autowired
    public HarvestService(HarvestResultRepository harvestResultRepository,
                          PlantationService plantationService,
                          PlantationValidationService plantationValidationService,
                          HarvestEventPublisher eventPublisher,
                          List<HarvestValidator> validators,
                          WorkerAssignmentRepository workerAssignmentRepository,
                          PlantationAssignmentRepository plantationAssignmentRepository) {
        this.harvestResultRepository = harvestResultRepository;
        this.plantationService = plantationService;
        this.plantationValidationService = plantationValidationService;
        this.eventPublisher = eventPublisher;
        this.validators = validators;
        this.workerAssignmentRepository = workerAssignmentRepository;
        this.plantationAssignmentRepository = plantationAssignmentRepository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public HarvestResult submitHarvest(UUID workerId, HarvestRequestDto request) {
        // Eksekusi semua validasi secara dinamis (Strategy Pattern)
        for (HarvestValidator validator : validators) {
            validator.validate(workerId, request);
        }
        validateWorkerSubmissionScope(workerId, request);

        Plantation plantation = new Plantation();
        plantation.setId(request.getPlantationId());

        HarvestResult result = HarvestResult.builder()
                .workerId(workerId)
                .mandorId(request.getMandorId())
                .plantation(plantation)
                .harvestDate(request.getHarvestDate())
                .kgHarvested(request.getKgHarvested())
                .notes(request.getNotes())
                .readyForDelivery(false)
                .status("PENDING")
                .build();

        if (request.getPhotos() != null && !request.getPhotos().isEmpty()) {
            var photos = request.getPhotos().stream().map(p -> HarvestPhoto.builder()
                    .harvestResult(result)
                    .url(p.getUrl())
                    .filename(p.getFilename())
                    .sizeBytes(p.getSizeBytes())
                    .build()).collect(Collectors.toList());
            result.setPhotos(photos);
        }

        HarvestResult saved = harvestResultRepository.save(result);
        
        eventPublisher.publishHarvestSubmitted(new HarvestSubmittedEvent(
                saved.getId(),
                saved.getWorkerId().toString(),
                saved.getPlantationId().toString(),
                saved.getKgHarvested(),
                Instant.now()
        ));
        
        logger.info("Harvest submitted: id={}, workerId={}, plantationId={}", saved.getId(), workerId, request.getPlantationId());
        return saved;
    }

    private void validateWorkerSubmissionScope(UUID workerId, HarvestRequestDto request) {
        WorkerAssignment assignment = workerAssignmentRepository.findById(workerId)
                .orElseThrow(() -> new IllegalStateException("Buruh belum ditugaskan ke Mandor"));

        if (!assignment.getMandorId().equals(request.getMandorId())) {
            throw new IllegalStateException("Buruh hanya dapat mengirim panen ke Mandor yang ditugaskan");
        }

        boolean mandorAssignedToPlantation = plantationAssignmentRepository
                .findByPersonnelIdAndRole(request.getMandorId(), PlantationAssignment.PersonnelRole.MANDOR)
                .stream()
                .anyMatch(plantationAssignment -> plantationAssignment.getPlantationId().equals(request.getPlantationId()));
        if (!mandorAssignedToPlantation) {
            throw new IllegalStateException("Mandor belum ditugaskan ke kebun yang dipilih");
        }
    }

    @Transactional
    public HarvestResult validateHarvest(@NonNull UUID mandorId, @NonNull UUID harvestId, @NonNull ValidationRequestDto request) {
        if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Status validasi tidak boleh kosong");
        }

        HarvestResult harvest = harvestResultRepository.findById(harvestId)
                .orElseThrow(() -> new IllegalArgumentException("Laporan Harvest tidak ditemukan"));

        if (!"PENDING".equals(harvest.getStatus())) {
            throw new IllegalStateException("Laporan ini sudah divalidasi dan tidak dapat diubah lagi.");
        }

        if (!workerAssignmentRepository.existsByWorkerIdAndMandorId(harvest.getWorkerId(), mandorId)) {
            throw new IllegalStateException("Akses Ditolak: Buruh ini bukan di bawah pengawasan Anda!");
        }

        if ("REJECTED".equals(request.getStatus()) && (request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty())) {
            throw new IllegalArgumentException("Alasan penolakan wajib diisi jika menolak laporan.");
        }

        harvest.setStatus(request.getStatus());
        harvest.setRejectionReason(request.getRejectionReason());
        harvest.setValidatedAt(LocalDateTime.now());

        if ("APPROVED".equals(request.getStatus())) {
            harvest.setReadyForDelivery(true);
            eventPublisher.publishHarvestApproved(new HarvestApprovedEvent(
                    harvest.getId(),
                    harvest.getWorkerId().toString(),
                    harvest.getMandorId().toString(),
                    harvest.getPlantation().getId().toString(),
                    harvest.getKgHarvested(),
                    Instant.now()
            ));
        }

        logger.info("Harvest validated: id={}, status={}", harvestId, request.getStatus());
        return harvestResultRepository.save(harvest);
    }

    public HarvestResult getHarvestById(UUID harvestId) {
        return harvestResultRepository.findById(harvestId)
                .orElseThrow(() -> new IllegalArgumentException("Laporan Harvest tidak ditemukan"));
    }

    public List<HarvestResult> getHarvestsByWorkerId(UUID workerId) {
        if (workerId == null) {
            throw new IllegalArgumentException("Worker ID tidak boleh kosong");
        }
        return harvestResultRepository.findByWorkerId(workerId);
    }

    public List<HarvestResult> getAllHarvests() {
        return harvestResultRepository.findAll();
    }

    public List<HarvestResult> getBuruhHistory(UUID workerId, LocalDate startDate, LocalDate endDate, String status) {
        if (workerId == null) {
            throw new IllegalArgumentException("Worker ID tidak boleh kosong");
        }
        return harvestResultRepository.findBuruhHistory(workerId, startDate, endDate, status);
    }

    public List<HarvestResult> getMandorHistory(UUID mandorId, LocalDate date, UUID filterWorkerId) {
        if (mandorId == null) {
            throw new IllegalArgumentException("Mandor ID tidak boleh kosong");
        }

        List<UUID> assignedWorkerIds = workerAssignmentRepository.findByMandorId(mandorId).stream()
                .map(WorkerAssignment::getWorkerId)
                .toList();
        if (assignedWorkerIds.isEmpty()) {
            return Collections.emptyList();
        }

        if (filterWorkerId != null) {
            if (!assignedWorkerIds.contains(filterWorkerId)) {
                return Collections.emptyList();
            }
            return harvestResultRepository.findMandorHistory(date, filterWorkerId).stream()
                    .filter(harvest -> Objects.equals(harvest.getMandorId(), mandorId))
                    .toList();
        }

        return harvestResultRepository.findMandorHistoryByWorkerIds(assignedWorkerIds, date).stream()
                .filter(harvest -> Objects.equals(harvest.getMandorId(), mandorId))
                .toList();
    }
}
