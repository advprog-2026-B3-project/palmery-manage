package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.dto.HarvestRequestDto;
import id.ac.ui.cs.advprog.palmerymanage.dto.ValidationRequestDto;
import id.ac.ui.cs.advprog.palmerymanage.event.HarvestEventPublisher;
import id.ac.ui.cs.advprog.palmerymanage.event.HarvestApprovedEvent;
import id.ac.ui.cs.advprog.palmerymanage.model.HarvestResult;
import id.ac.ui.cs.advprog.palmerymanage.model.HarvestPhoto;
import id.ac.ui.cs.advprog.palmerymanage.model.Plantation;
import id.ac.ui.cs.advprog.palmerymanage.repository.HarvestResultRepository;
import id.ac.ui.cs.advprog.palmerymanage.repository.PlantationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HarvestService {

    private final HarvestResultRepository harvestResultRepository;
    private final PlantationRepository plantationRepository;
    private final HarvestEventPublisher eventPublisher;
    private final RestClient restClient;

    @Value("${assignment.api.url:http://localhost:8080/api/assignment}")
    private String assignmentApiUrl;

    @Value("${assignment.api.dummy:true}")
    private boolean useDummyAssignment;

    public HarvestService(HarvestResultRepository harvestResultRepository,
                          PlantationRepository plantationRepository,
                          HarvestEventPublisher eventPublisher) {
        this.harvestResultRepository = harvestResultRepository;
        this.plantationRepository = plantationRepository;
        this.eventPublisher = eventPublisher;
        this.restClient = RestClient.create();
    }

    @Transactional
    public HarvestResult submitHarvest(UUID workerId, HarvestRequestDto request) {
        // Validasi input
        if (request.getPlantationId() == null) {
            throw new IllegalArgumentException("ID Kebun (plantationId) tidak boleh kosong");
        }
        if (request.getMandorId() == null) {
            throw new IllegalArgumentException("ID Mandor (mandorId) tidak boleh kosong");
        }
        if (request.getHarvestDate() == null) {
            throw new IllegalArgumentException("Tanggal Harvest (harvestDate) tidak boleh kosong");
        }
        if (request.getKgHarvested() == null || request.getKgHarvested() < 1) {
            throw new IllegalArgumentException("Berat Harvest harus diisi dan minimal 1 kg");
        }
        if (request.getNotes() == null || request.getNotes().trim().isEmpty()) {
            throw new IllegalArgumentException("Catatan (notes) tidak boleh kosong");
        }

        // Validasi plantation exists
        if (!plantationRepository.existsById(request.getPlantationId())) {
            throw new IllegalArgumentException("Kebun dengan ID " + request.getPlantationId() + " tidak ditemukan");
        }

        // Guard: 1x sehari per buruh
        if (harvestResultRepository.existsByWorkerIdAndHarvestDate(workerId, request.getHarvestDate())) {
            throw new IllegalArgumentException("Buruh sudah melaporkan Harvest pada tanggal ini.");
        }

        Plantation plantation = plantationRepository.findById(request.getPlantationId())
                .orElseThrow(() -> new IllegalArgumentException("Kebun tidak ditemukan"));

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

        // Menyambungkan data foto dari Rustfs
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
        log.info("Harvest submitted: id={}, workerId={}, plantationId={}", saved.getId(), workerId, request.getPlantationId());
        return saved;
    }

    private boolean checkIsAnakBuah(UUID mandorId, UUID workerId) {
        if (useDummyAssignment) {
            log.debug("[DUMMY] Checking if worker {} is under mandor {}", workerId, mandorId);
            return true;
        }

        try {
            String url = assignmentApiUrl + "/check?mandorId=" + mandorId + "&workerId=" + workerId;
            log.debug("Calling assignment API: {}", url);

            Boolean isAnakBuah = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(Boolean.class);

            return Boolean.TRUE.equals(isAnakBuah);
        } catch (Exception e) {
            log.error("Failed to call assignment API: {}", e.getMessage());
            return false;
        }
    }

    @Transactional
    public HarvestResult validateHarvest(@NonNull UUID mandorId, @NonNull UUID harvestId, @NonNull ValidationRequestDto request) {

        if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Status validasi tidak boleh kosong");
        }

        HarvestResult harvest = harvestResultRepository.findById(harvestId)
                .orElseThrow(() -> new IllegalArgumentException("Laporan Harvest tidak ditemukan"));

        // Data Immutability
        if (!"PENDING".equals(harvest.getStatus())) {
            throw new IllegalStateException("Laporan ini sudah divalidasi dan tidak dapat diubah lagi.");
        }

        // Guard: mandor hanya bisa validasi anak buahnya
        if (!checkIsAnakBuah(mandorId, harvest.getWorkerId())) {
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

        log.info("Harvest validated: id={}, status={}", harvestId, request.getStatus());
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

    public List<HarvestResult> getMandorHistory(LocalDate date, UUID filterWorkerId) {
        return harvestResultRepository.findMandorHistory(date, filterWorkerId);
    }
}
