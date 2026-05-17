package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.dto.HarvestSummaryDto;
import id.ac.ui.cs.advprog.palmerymanage.model.HarvestResult;
import id.ac.ui.cs.advprog.palmerymanage.repository.HarvestResultRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementasi dari HarvestProviderService.
 *
 * Class ini HANYA berinteraksi langsung dengan HarvestResultRepository.
 * Semua logika bisnis internal (submit, validate, dll) tetap berada di HarvestService.
 * Class ini hanya bertanggung jawab untuk "membaca" data dan mengeksposnya
 * dalam bentuk DTO yang aman untuk digunakan modul lain.
 */
@Service
public class HarvestProviderServiceImpl implements HarvestProviderService {

    private final HarvestResultRepository harvestResultRepository;

    public HarvestProviderServiceImpl(HarvestResultRepository harvestResultRepository) {
        this.harvestResultRepository = harvestResultRepository;
    }

    /**
     * Memetakan HarvestResult (entitas internal) ke HarvestSummaryDto (kontrak publik).
     * Method ini adalah "firewall" data: hanya field yang aman yang diteruskan keluar.
     */
    private HarvestSummaryDto toSummary(HarvestResult harvest) {
        return HarvestSummaryDto.builder()
                .harvestId(harvest.getId())
                .workerId(harvest.getWorkerId())
                .mandorId(harvest.getMandorId())
                .plantationId(harvest.getPlantationId())
                .harvestDate(harvest.getHarvestDate())
                .kgHarvested(harvest.getKgHarvested())
                .status(harvest.getStatus())
                .readyForDelivery(Boolean.TRUE.equals(harvest.getReadyForDelivery()))
                .build();
    }

    @Override
    public boolean isReadyForDelivery(UUID harvestId) {
        return harvestResultRepository.findById(harvestId)
                .map(h -> Boolean.TRUE.equals(h.getReadyForDelivery()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Laporan panen dengan ID " + harvestId + " tidak ditemukan."));
    }

    @Override
    public String getHarvestStatus(UUID harvestId) {
        return harvestResultRepository.findById(harvestId)
                .map(HarvestResult::getStatus)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Laporan panen dengan ID " + harvestId + " tidak ditemukan."));
    }

    @Override
    public Optional<HarvestSummaryDto> findHarvestSummary(UUID harvestId) {
        return harvestResultRepository.findById(harvestId)
                .map(this::toSummary);
    }

    @Override
    public List<HarvestSummaryDto> getApprovedHarvestsByWorker(UUID workerId) {
        return harvestResultRepository.findByWorkerId(workerId).stream()
                .filter(h -> "APPROVED".equals(h.getStatus()))
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    public List<HarvestSummaryDto> getApprovedHarvestsByPlantation(UUID plantationId) {
        return harvestResultRepository.findByPlantation_Id(plantationId).stream()
                .filter(h -> "APPROVED".equals(h.getStatus()))
                .map(this::toSummary)
                .collect(Collectors.toList());
    }
}
