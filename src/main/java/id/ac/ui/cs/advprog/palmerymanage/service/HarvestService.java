package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.dto.HarvestRequestDto;
import id.ac.ui.cs.advprog.palmerymanage.dto.ValidationRequestDto;
import id.ac.ui.cs.advprog.palmerymanage.event.HarvestEventPublisher;
import id.ac.ui.cs.advprog.palmerymanage.event.HarvestApprovedEvent;
import id.ac.ui.cs.advprog.palmerymanage.model.HarvestResult;
import id.ac.ui.cs.advprog.palmerymanage.model.HarvestPhoto;
import id.ac.ui.cs.advprog.palmerymanage.repository.HarvestResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
public class HarvestService {

    private final HarvestResultRepository harvestResultRepository;
    private final HarvestEventPublisher eventPublisher;

    public HarvestService(HarvestResultRepository harvestResultRepository,
                          HarvestEventPublisher eventPublisher) {
        this.harvestResultRepository = harvestResultRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public HarvestResult submitHarvest(UUID workerId, HarvestRequestDto request) {
        // validasi manual
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

        // Buruh hanya boleh 1x sehari create harvest
        if (harvestResultRepository.existsByWorkerIdAndHarvestDate(workerId, request.getHarvestDate())) {
            throw new IllegalArgumentException("Buruh sudah melaporkan Harvest pada tanggal ini.");
        }

        HarvestResult result = HarvestResult.builder()
                .workerId(workerId)
                .mandorId(request.getMandorId())
                .plantationId(request.getPlantationId())
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

    // mock api
    private boolean checkIsAnakBuah(UUID mandorId, UUID workerId) {
        // TODO: ganti HTTP Call ke API Assignment
        System.out.println("[MOCKING] Mengecek apakah Buruh " + workerId + " adalah bawahan Mandor " + mandorId);
        return true; // sementara selalu true
    }

    @Transactional
    public HarvestResult validateHarvest(UUID mandorId, UUID harvestId, ValidationRequestDto request) {

        if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Status validasi tidak boleh kosong");
        }

        HarvestResult harvest = harvestResultRepository.findById(harvestId)
                .orElseThrow(() -> new IllegalArgumentException("Laporan Harvest tidak ditemukan"));

        // Data Immutability
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

            //Emit Spring Event HarvestApproved
            eventPublisher.publishHarvestApproved(new HarvestApprovedEvent(
                    harvest.getId(),
                    harvest.getWorkerId().toString(),
                    harvest.getMandorId().toString(),
                    harvest.getPlantationId().toString(),
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