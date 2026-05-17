package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.dto.HarvestRequestDto;
import id.ac.ui.cs.advprog.palmerymanage.dto.ValidationRequestDto;
import id.ac.ui.cs.advprog.palmerymanage.event.HarvestEventPublisher;
import id.ac.ui.cs.advprog.palmerymanage.event.HarvestApprovedEvent;
import id.ac.ui.cs.advprog.palmerymanage.model.HarvestResult;
import id.ac.ui.cs.advprog.palmerymanage.model.HarvestPhoto;
import id.ac.ui.cs.advprog.palmerymanage.model.Plantation;
import id.ac.ui.cs.advprog.palmerymanage.repository.HarvestResultRepository;
import id.ac.ui.cs.advprog.palmerymanage.service.PlantationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;
import org.springframework.lang.NonNull;

@Service
public class HarvestService {

    private final HarvestResultRepository harvestResultRepository;
    private final PlantationService plantationService;
    private final PlantationValidationService plantationValidationService;
    private final HarvestEventPublisher eventPublisher;
    private final RestClient restClient;

    @Value("${assignment.api.url:http://localhost:8080/api/assignment}")
    private String assignmentApiUrl;
    
    // Toggle on/off untuk dummy. Secara default berjalan dalam mode dummy (true)
    @Value("${assignment.api.dummy:true}")
    private boolean useDummyAssignment;

    public HarvestService(HarvestResultRepository harvestResultRepository,
                          PlantationService plantationService,
                          PlantationValidationService plantationValidationService,
                          HarvestEventPublisher eventPublisher) {
        this.harvestResultRepository = harvestResultRepository;
        this.plantationService = plantationService;
        this.plantationValidationService = plantationValidationService;
        this.eventPublisher = eventPublisher;
        this.restClient = RestClient.create();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public HarvestResult submitHarvest(UUID workerId, HarvestRequestDto request) {

        // [OPTIMASI #2] Fail-Fast: Semua validasi input ringan dieksekusi LEBIH DULU
        // sebelum menyentuh database sama sekali. Jika request tidak valid,
        // kita tolak di sini tanpa overhead apapun.
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

        // [OPTIMASI #3] Validasi kebun via Spring Cache (Caffeine, TTL 30 menit).
        // Jika plantationId sudah pernah diverifikasi dan belum expire →
        // langsung lewati HTTP call ke PlantationService (< 1ms dari memory).
        // Jika cache expire atau belum ada → panggil PlantationService 1x, simpan ke cache.
        plantationValidationService.validateAndCachePlantation(request.getPlantationId());

        // DB Call: Cek duplikasi panen (tidak bisa di-cache karena data berubah setiap hari)
        if (harvestResultRepository.existsByWorkerIdAndHarvestDate(workerId, request.getHarvestDate())) {
            throw new IllegalArgumentException("Buruh sudah melaporkan Harvest pada tanggal ini.");
        }

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

        // menyambungkan data foto dari Rustfs
        if (request.getPhotos() != null && !request.getPhotos().isEmpty()) {
            var photos = request.getPhotos().stream().map(p -> HarvestPhoto.builder()
                    .harvestResult(result)
                    .url(p.getUrl())
                    .filename(p.getFilename())
                    .sizeBytes(p.getSizeBytes())
                    .build()).collect(Collectors.toList());
            result.setPhotos(photos);
        }

        return harvestResultRepository.save(result);
    }

    private boolean checkIsAnakBuah(UUID mandorId, UUID workerId) {
        // mode dummy aktif langsung return true
        if (useDummyAssignment) {
            System.out.println("[MOCKING/DUMMY] Mengecek apakah Buruh " + workerId + " adalah bawahan Mandor " + mandorId);
            return true;
        }

        try {
            String url = assignmentApiUrl + "/check?mandorId=" + mandorId + "&workerId=" + workerId;
            
            System.out.println("[API CALL] Mengecek asignment ke: " + url);
            
            Boolean isAnakBuah = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(Boolean.class);
                    
            return Boolean.TRUE.equals(isAnakBuah);
        } catch (Exception e) {
            System.err.println("[ERROR] Gagal memanggil API Assignment: " + e.getMessage());
            // Default ke false 
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

        //data Immutability
        if (!"PENDING".equals(harvest.getStatus())) {
            throw new IllegalStateException("Laporan ini sudah divalidasi dan tidak dapat diubah lagi.");
        }

        // tambahan guard mandor
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

        return harvestResultRepository.save(harvest);
    }

    public HarvestResult getHarvestById(UUID harvestId) {
        return harvestResultRepository.findById(harvestId)
                .orElseThrow(() -> new IllegalArgumentException("Laporan Harvest tidak ditemukan"));
    }

    //Get riwayat Harvest berdasarkan workerId
    public List<HarvestResult> getHarvestsByWorkerId(UUID workerId) {
        if (workerId == null) {
            throw new IllegalArgumentException("Worker ID tidak boleh kosong");
        }
        return harvestResultRepository.findByWorkerId(workerId);
    }

    public List<HarvestResult> getAllHarvests() {
        return harvestResultRepository.findAll();
    }

    // service untuk Riwayat panen Buruh
    public List<HarvestResult> getBuruhHistory(UUID workerId, LocalDate startDate, LocalDate endDate, String status) {
        if (workerId == null) {
            throw new IllegalArgumentException("Worker ID tidak boleh kosong");
        }
        return harvestResultRepository.findBuruhHistory(workerId, startDate, endDate, status);
    }

    // Service untuk Riwayat Mandor (Semua Buruh)
    public List<HarvestResult> getMandorHistory(LocalDate date, UUID filterWorkerId) {
        return harvestResultRepository.findMandorHistory(date, filterWorkerId);
    }
}