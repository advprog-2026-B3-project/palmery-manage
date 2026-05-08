package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.dto.CreatePengirimanRequest;
import id.ac.ui.cs.advprog.palmerymanage.exception.BadRequestException;
import id.ac.ui.cs.advprog.palmerymanage.exception.ForbiddenException;
import id.ac.ui.cs.advprog.palmerymanage.exception.OverWeightException;
import id.ac.ui.cs.advprog.palmerymanage.model.Delivery;
import id.ac.ui.cs.advprog.palmerymanage.model.DeliveryStatus;
import id.ac.ui.cs.advprog.palmerymanage.repository.DeliveryRepository;
import id.ac.ui.cs.advprog.palmerymanage.model.HarvestResult;
import id.ac.ui.cs.advprog.palmerymanage.repository.HarvestResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DeliveryService {

    static final int MAX_TOTAL_KG = 400;

    private final DeliveryRepository deliveryRepository;
    private final HarvestResultRepository harvestResultRepository;
    private final DeliveryEventPublisher eventPublisher;

    public DeliveryService(DeliveryRepository deliveryRepository,
                           HarvestResultRepository harvestResultRepository,
                           DeliveryEventPublisher eventPublisher) {
        this.deliveryRepository = deliveryRepository;
        this.harvestResultRepository = harvestResultRepository;
        this.eventPublisher = eventPublisher;
    }

    public Delivery createPengiriman(String mandorId, CreatePengirimanRequest request) {
        if (mandorId == null || mandorId.isBlank()) {
            throw new BadRequestException("Mandor tidak ditemukan");
        }

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

        boolean hasNonReady = panenList.stream().anyMatch(panen -> !Boolean.TRUE.equals(panen.getReadyForDelivery()));
        if (hasNonReady) {
            throw new BadRequestException("Semua hasil panen harus berstatus Siap Angkut");
        }

        int total = (int) Math.round(panenList.stream().mapToDouble(p -> p.getKgHarvested() == null ? 0.0 : p.getKgHarvested()).sum());
        if (total > MAX_TOTAL_KG) {
            throw new OverWeightException("Total berat maksimum 400 kg");
        }

        // constraint kepemilikan (mandor hanya boleh pilih panen yang dia validasi)
        boolean notOwned = panenList.stream().anyMatch(p -> p.getMandorId() == null || !p.getMandorId().toString().equals(mandorId));
        if (notOwned) {
            throw new ForbiddenException("Panen tidak berada di bawah mandor ini");
        }

        Delivery delivery = new Delivery();
        delivery.setSupirId(request.supirId());
        delivery.setMandorId(mandorId);
        // Simpan plantationId dari panen pertama sebagai kebun_id untuk filtering sederhana
        delivery.setKebunId(panenList.getFirst().getPlantationId().toString());
        delivery.setTotalKg(total);
        delivery.setPanenIds(request.panenIds());
        delivery.setStatus(DeliveryStatus.MEMUAT);

        return deliveryRepository.save(delivery);
    }

    public List<Delivery> pengirimanAktifSupir(String supirId) {
        List<DeliveryStatus> aktif = List.of(
                DeliveryStatus.MEMUAT,
                DeliveryStatus.MENGIRIM,
                DeliveryStatus.TIBA_DI_TUJUAN,
                DeliveryStatus.PENDING_MANDOR_REVIEW,
                DeliveryStatus.APPROVED_MANDOR,
                DeliveryStatus.PENDING_ADMIN_REVIEW
        );
        return deliveryRepository.findBySupirIdAndStatusIn(supirId, aktif);
    }

    public List<Delivery> riwayatSupir(String supirId, LocalDate from, LocalDate to) {
        Instant fromInstant = from.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant toInstant = to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        return deliveryRepository.findBySupirIdAndCreatedAtBetween(supirId, fromInstant, toInstant);
    }

    public List<Delivery> pengirimanAktifKebun(String kebunId) {
        EnumSet<DeliveryStatus> aktif = EnumSet.complementOf(EnumSet.of(
                DeliveryStatus.REJECTED_ADMIN,
                DeliveryStatus.PARTIAL_REJECTED_ADMIN
        ));
        return deliveryRepository.findByKebunIdAndStatusIn(kebunId, List.copyOf(aktif));
    }

    public Delivery updateStatusSupir(String supirId, UUID id, DeliveryStatus target) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Pengiriman tidak ditemukan"));

        if (!delivery.getSupirId().equals(supirId)) {
            throw new ForbiddenException("Supir tidak berhak mengubah pengiriman ini");
        }

        DeliveryStatus current = delivery.getStatus();
        if (!isValidTransitionForDriver(current, target)) {
            throw new BadRequestException("Transisi status tidak valid");
        }

        delivery.setStatus(target);

        if (target == DeliveryStatus.TIBA_DI_TUJUAN) {
            eventPublisher.publishPengirimanTiba(delivery);
            delivery.setStatus(DeliveryStatus.PENDING_MANDOR_REVIEW);
        }

        return delivery;
    }

    boolean isValidTransitionForDriver(DeliveryStatus from, DeliveryStatus to) {
        return switch (from) {
            case MEMUAT -> to == DeliveryStatus.MENGIRIM;
            case MENGIRIM -> to == DeliveryStatus.TIBA_DI_TUJUAN;
            default -> false;
        };
    }

    public Delivery approveByMandor(String mandorId, UUID id) {
        Delivery delivery = requireOwnedByMandor(mandorId, id);
        if (delivery.getStatus() != DeliveryStatus.PENDING_MANDOR_REVIEW) {
            throw new BadRequestException("Pengiriman belum siap di-approve mandor");
        }
        delivery.setStatus(DeliveryStatus.PENDING_ADMIN_REVIEW);
        eventPublisher.publishPengirimanApprovedMandor(delivery);
        return delivery;
    }

    public Delivery rejectByMandor(String mandorId, UUID id, String reason) {
        Delivery delivery = requireOwnedByMandor(mandorId, id);
        delivery.setStatus(DeliveryStatus.REJECTED_MANDOR);
        delivery.setRejectedReason(reason);
        return delivery;
    }

    public List<Delivery> pendingAdmin() {
        return deliveryRepository.findByStatus(DeliveryStatus.PENDING_ADMIN_REVIEW);
    }

    public Delivery getById(UUID id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Pengiriman tidak ditemukan"));
    }

    public Delivery approveByAdmin(UUID id) {
        Delivery delivery = getById(id);
        delivery.setStatus(DeliveryStatus.APPROVED_ADMIN);
        eventPublisher.publishPengirimanApprovedAdmin(delivery, delivery.getTotalKg());
        return delivery;
    }

    public Delivery partialRejectByAdmin(UUID id, int recognizedKg, String reason) {
        Delivery delivery = getById(id);

        if (recognizedKg <= 0 || recognizedKg > delivery.getTotalKg()) {
            throw new BadRequestException("Berat yang diakui harus > 0 dan <= total");
        }

        delivery.setStatus(DeliveryStatus.PARTIAL_REJECTED_ADMIN);
        delivery.setRecognizedKg(recognizedKg);
        delivery.setRejectedReason(reason);
        eventPublisher.publishPengirimanApprovedAdmin(delivery, recognizedKg);
        return delivery;
    }

    public Delivery rejectByAdmin(UUID id, String reason) {
        Delivery delivery = getById(id);

        delivery.setStatus(DeliveryStatus.REJECTED_ADMIN);
        delivery.setRejectedReason(reason);
        return delivery;
    }

    private Delivery requireOwnedByMandor(String mandorId, UUID id) {
        Delivery delivery = getById(id);
        if (!delivery.getMandorId().equals(mandorId)) {
            throw new ForbiddenException("Mandor tidak berhak mengubah pengiriman ini");
        }
        return delivery;
    }
}
