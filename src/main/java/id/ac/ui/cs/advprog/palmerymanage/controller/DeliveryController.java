package id.ac.ui.cs.advprog.palmerymanage.controller;

import id.ac.ui.cs.advprog.palmerymanage.dto.CreatePengirimanRequest;
import id.ac.ui.cs.advprog.palmerymanage.dto.PartialRejectRequest;
import id.ac.ui.cs.advprog.palmerymanage.dto.RejectRequest;
import id.ac.ui.cs.advprog.palmerymanage.dto.UpdateStatusRequest;
import id.ac.ui.cs.advprog.palmerymanage.model.Delivery;
import id.ac.ui.cs.advprog.palmerymanage.model.DeliveryStatus;
import id.ac.ui.cs.advprog.palmerymanage.model.Driver;
import id.ac.ui.cs.advprog.palmerymanage.model.Harvest;
import id.ac.ui.cs.advprog.palmerymanage.model.Mandor;
import id.ac.ui.cs.advprog.palmerymanage.repository.DriverRepository;
import id.ac.ui.cs.advprog.palmerymanage.repository.HarvestRepository;
import id.ac.ui.cs.advprog.palmerymanage.repository.MandorRepository;
import id.ac.ui.cs.advprog.palmerymanage.service.DeliveryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final DriverRepository driverRepository;
    private final MandorRepository mandorRepository;
    private final HarvestRepository harvestRepository;

    public DeliveryController(DeliveryService deliveryService,
                              DriverRepository driverRepository,
                              MandorRepository mandorRepository,
                              HarvestRepository harvestRepository) {
        this.deliveryService = deliveryService;
        this.driverRepository = driverRepository;
        this.mandorRepository = mandorRepository;
        this.harvestRepository = harvestRepository;
    }

    private String currentUserId(String header) {
        return header == null || header.isBlank() ? "MDR-1" : header;
    }

    // Mandor: GET /mandor/drivers?search=
    @GetMapping("/mandor/drivers")
    public ResponseEntity<List<Map<String, Object>>> driversForMandor(
            @RequestHeader(value = "X-User-Id", required = false) String mandorIdHeader,
            @RequestParam(value = "search", required = false, defaultValue = "") String search) {
        String mandorId = currentUserId(mandorIdHeader);
        Mandor mandor = mandorRepository.findById(mandorId).orElseThrow();
        List<Driver> drivers = driverRepository.findByKebunIdAndNamaContainingIgnoreCase(
                mandor.getKebunId(), search);
        List<Map<String, Object>> body = drivers.stream().map(driver -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", driver.getId());
            m.put("nama", driver.getNama());
            m.put("kebun_id", driver.getKebunId());
            m.put("kontak", driver.getKontak());
            return m;
        }).toList();
        return ResponseEntity.ok(body);
    }

    // Mandor: panen siap angkut
    @GetMapping("/mandor/panen/siap-angkut")
    public ResponseEntity<List<Map<String, Object>>> panenSiapAngkut(
            @RequestHeader(value = "X-User-Id", required = false) String mandorIdHeader) {
        String mandorId = currentUserId(mandorIdHeader);
        Mandor mandor = mandorRepository.findById(mandorId).orElseThrow();
        List<Harvest> panen = harvestRepository.findByKebunIdAndReadyForDeliveryIsTrue(mandor.getKebunId());
        List<Map<String, Object>> body = panen.stream().map(h -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", h.getId());
            m.put("berat_kg", h.getBeratKg());
            m.put("kebun_id", h.getKebunId());
            m.put("mandor_id", h.getMandorId());
            m.put("status", h.getStatus());
            return m;
        }).toList();
        return ResponseEntity.ok(body);
    }

    // Mandor: buat pengiriman baru
    @PostMapping("/mandor/pengiriman")
    public ResponseEntity<Map<String, Object>> createPengiriman(
            @RequestHeader(value = "X-User-Id", required = false) String mandorIdHeader,
            @Valid @RequestBody CreatePengirimanRequest request) {
        String mandorId = currentUserId(mandorIdHeader);
        Delivery delivery = deliveryService.createPengiriman(mandorId, request);
        return ResponseEntity.ok(toDeliveryResponse(delivery));
    }

    // Supir: list pengiriman aktif
    @GetMapping("/supir/pengiriman/aktif")
    public ResponseEntity<List<Map<String, Object>>> pengirimanAktifSupir(
            @RequestHeader(value = "X-User-Id", required = false) String supirIdHeader) {
        String supirId = supirIdHeader == null || supirIdHeader.isBlank() ? "DRV-1" : supirIdHeader;
        List<Delivery> list = deliveryService.pengirimanAktifSupir(supirId);
        return ResponseEntity.ok(list.stream().map(this::toDeliveryResponse).toList());
    }

    // Supir: riwayat
    @GetMapping("/supir/pengiriman/riwayat")
    public ResponseEntity<List<Map<String, Object>>> riwayatSupir(
            @RequestHeader(value = "X-User-Id", required = false) String supirIdHeader,
            @RequestParam("from") String from,
            @RequestParam("to") String to) {
        String supirId = supirIdHeader == null || supirIdHeader.isBlank() ? "DRV-1" : supirIdHeader;
        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate = LocalDate.parse(to);
        List<Delivery> list = deliveryService.riwayatSupir(supirId, fromDate, toDate);
        return ResponseEntity.ok(list.stream().map(this::toDeliveryResponse).toList());
    }

    // Supir: update status
    @PatchMapping("/supir/pengiriman/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatusSupir(
            @RequestHeader(value = "X-User-Id", required = false) String supirIdHeader,
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateStatusRequest request) {
        String supirId = supirIdHeader == null || supirIdHeader.isBlank() ? "DRV-1" : supirIdHeader;
        DeliveryStatus target = DeliveryStatus.valueOf(request.status());
        Delivery delivery = deliveryService.updateStatusSupir(supirId, id, target);
        return ResponseEntity.ok(toDeliveryResponse(delivery));
    }

    // Mandor: monitor truk
    @GetMapping("/mandor/pengiriman/aktif")
    public ResponseEntity<List<Map<String, Object>>> pengirimanAktifMandor(
            @RequestHeader(value = "X-User-Id", required = false) String mandorIdHeader) {
        String mandorId = currentUserId(mandorIdHeader);
        Mandor mandor = mandorRepository.findById(mandorId).orElseThrow();
        List<Delivery> list = deliveryService.pengirimanAktifKebun(mandor.getKebunId());
        return ResponseEntity.ok(list.stream().map(this::toDeliveryResponse).toList());
    }

    // Mandor: approve / reject
    @PostMapping("/mandor/pengiriman/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveMandor(
            @RequestHeader(value = "X-User-Id", required = false) String mandorIdHeader,
            @PathVariable("id") UUID id) {
        String mandorId = currentUserId(mandorIdHeader);
        Delivery delivery = deliveryService.approveByMandor(mandorId, id);
        return ResponseEntity.ok(toDeliveryResponse(delivery));
    }

    @PostMapping("/mandor/pengiriman/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectMandor(
            @RequestHeader(value = "X-User-Id", required = false) String mandorIdHeader,
            @PathVariable("id") UUID id,
            @Valid @RequestBody RejectRequest request) {
        String mandorId = currentUserId(mandorIdHeader);
        Delivery delivery = deliveryService.rejectByMandor(mandorId, id, request.reason());
        return ResponseEntity.ok(toDeliveryResponse(delivery));
    }

    // Admin
    @GetMapping("/admin/pengiriman/pending")
    public ResponseEntity<List<Map<String, Object>>> pendingAdmin() {
        List<Delivery> list = deliveryService.pendingAdmin();
        return ResponseEntity.ok(list.stream().map(this::toDeliveryResponse).toList());
    }

    @PostMapping("/admin/pengiriman/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveAdmin(@PathVariable("id") UUID id) {
        Delivery delivery = deliveryService.approveByAdmin(id);
        return ResponseEntity.ok(toDeliveryResponse(delivery));
    }

    @PostMapping("/admin/pengiriman/{id}/partial-reject")
    public ResponseEntity<Map<String, Object>> partialRejectAdmin(
            @PathVariable("id") UUID id,
            @Valid @RequestBody PartialRejectRequest request) {
        Delivery delivery = deliveryService.partialRejectByAdmin(id, request.recognizedKg(), request.reason());
        return ResponseEntity.ok(toDeliveryResponse(delivery));
    }

    @PostMapping("/admin/pengiriman/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectAdmin(
            @PathVariable("id") UUID id,
            @Valid @RequestBody RejectRequest request) {
        Delivery delivery = deliveryService.rejectByAdmin(id, request.reason());
        return ResponseEntity.ok(toDeliveryResponse(delivery));
    }

    private Map<String, Object> toDeliveryResponse(Delivery delivery) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", delivery.getId());
        map.put("supir_id", delivery.getSupirId());
        map.put("mandor_id", delivery.getMandorId());
        map.put("kebun_id", delivery.getKebunId());
        map.put("total_kg", delivery.getTotalKg());
        map.put("status", delivery.getStatus().name());
        map.put("panen_ids", delivery.getPanenIds());
        map.put("rejected_reason", delivery.getRejectedReason());
        map.put("recognized_kg", delivery.getRecognizedKg());
        map.put("created_at", delivery.getCreatedAt());
        map.put("updated_at", delivery.getUpdatedAt());
        return map;
    }
}

