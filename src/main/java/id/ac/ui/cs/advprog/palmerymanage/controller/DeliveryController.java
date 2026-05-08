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
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
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

    private String resolveUserId(Authentication authentication, String headerFallback, String defaultFallback) {
        if (authentication != null && authentication.getName() != null && !authentication.getName().isBlank()) {
            return authentication.getName();
        }
        if (headerFallback != null && !headerFallback.isBlank()) {
            return headerFallback;
        }
        return defaultFallback;
    }

    // Mandor: GET /mandor/drivers?search=
    @GetMapping("/mandor/drivers")
    @PreAuthorize("hasRole('MANDOR')")
    public ResponseEntity<List<Map<String, Object>>> driversForMandor(
            @RequestHeader(value = "X-User-Id", required = false) String mandorIdHeader,
            Authentication authentication,
            @RequestParam(value = "search", required = false, defaultValue = "") String search) {
        String mandorId = resolveUserId(authentication, mandorIdHeader, "MDR-1");
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
    @PreAuthorize("hasRole('MANDOR')")
    public ResponseEntity<List<Map<String, Object>>> panenSiapAngkut(
            @RequestHeader(value = "X-User-Id", required = false) String mandorIdHeader,
            Authentication authentication) {
        String mandorId = resolveUserId(authentication, mandorIdHeader, "MDR-1");
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
    @PreAuthorize("hasRole('MANDOR')")
    public ResponseEntity<Map<String, Object>> createPengiriman(
            @RequestHeader(value = "X-User-Id", required = false) String mandorIdHeader,
            Authentication authentication,
            @Valid @RequestBody CreatePengirimanRequest request) {
        String mandorId = resolveUserId(authentication, mandorIdHeader, "MDR-1");
        Delivery delivery = deliveryService.createPengiriman(mandorId, request);
        return ResponseEntity.ok(toDeliveryResponse(delivery));
    }

    // Supir: list pengiriman aktif
    @GetMapping("/supir/pengiriman/aktif")
    @PreAuthorize("hasRole('SUPIR')")
    public ResponseEntity<List<Map<String, Object>>> pengirimanAktifSupir(
            @RequestHeader(value = "X-User-Id", required = false) String supirIdHeader,
            Authentication authentication) {
        String supirId = resolveUserId(authentication, supirIdHeader, "DRV-1");
        List<Delivery> list = deliveryService.pengirimanAktifSupir(supirId);
        return ResponseEntity.ok(list.stream().map(this::toDeliveryResponse).toList());
    }

    // Supir: riwayat
    @GetMapping("/supir/pengiriman/riwayat")
    @PreAuthorize("hasRole('SUPIR')")
    public ResponseEntity<List<Map<String, Object>>> riwayatSupir(
            @RequestHeader(value = "X-User-Id", required = false) String supirIdHeader,
            Authentication authentication,
            @RequestParam("from") String from,
            @RequestParam("to") String to) {
        String supirId = resolveUserId(authentication, supirIdHeader, "DRV-1");
        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate = LocalDate.parse(to);
        List<Delivery> list = deliveryService.riwayatSupir(supirId, fromDate, toDate);
        return ResponseEntity.ok(list.stream().map(this::toDeliveryResponse).toList());
    }

    // Supir: update status
    @PatchMapping("/supir/pengiriman/{id}/status")
    @PreAuthorize("hasRole('SUPIR')")
    public ResponseEntity<Map<String, Object>> updateStatusSupir(
            @RequestHeader(value = "X-User-Id", required = false) String supirIdHeader,
            Authentication authentication,
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateStatusRequest request) {
        String supirId = resolveUserId(authentication, supirIdHeader, "DRV-1");
        DeliveryStatus target = DeliveryStatus.valueOf(request.status());
        Delivery delivery = deliveryService.updateStatusSupir(supirId, id, target);
        return ResponseEntity.ok(toDeliveryResponse(delivery));
    }

    // Mandor: monitor truk
    @GetMapping("/mandor/pengiriman/aktif")
    @PreAuthorize("hasRole('MANDOR')")
    public ResponseEntity<List<Map<String, Object>>> pengirimanAktifMandor(
            @RequestHeader(value = "X-User-Id", required = false) String mandorIdHeader,
            Authentication authentication) {
        String mandorId = resolveUserId(authentication, mandorIdHeader, "MDR-1");
        Mandor mandor = mandorRepository.findById(mandorId).orElseThrow();
        List<Delivery> list = deliveryService.pengirimanAktifKebun(mandor.getKebunId());
        return ResponseEntity.ok(list.stream().map(this::toDeliveryResponse).toList());
    }

    // Mandor: approve / reject
    @PostMapping("/mandor/pengiriman/{id}/approve")
    @PreAuthorize("hasRole('MANDOR')")
    public ResponseEntity<Map<String, Object>> approveMandor(
            @RequestHeader(value = "X-User-Id", required = false) String mandorIdHeader,
            Authentication authentication,
            @PathVariable("id") UUID id) {
        String mandorId = resolveUserId(authentication, mandorIdHeader, "MDR-1");
        Delivery delivery = deliveryService.approveByMandor(mandorId, id);
        return ResponseEntity.ok(toDeliveryResponse(delivery));
    }

    @PostMapping("/mandor/pengiriman/{id}/reject")
    @PreAuthorize("hasRole('MANDOR')")
    public ResponseEntity<Map<String, Object>> rejectMandor(
            @RequestHeader(value = "X-User-Id", required = false) String mandorIdHeader,
            Authentication authentication,
            @PathVariable("id") UUID id,
            @Valid @RequestBody RejectRequest request) {
        String mandorId = resolveUserId(authentication, mandorIdHeader, "MDR-1");
        Delivery delivery = deliveryService.rejectByMandor(mandorId, id, request.reason());
        return ResponseEntity.ok(toDeliveryResponse(delivery));
    }

    // Admin
    @GetMapping("/admin/pengiriman/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> pendingAdmin() {
        List<Delivery> list = deliveryService.pendingAdmin();
        return ResponseEntity.ok(list.stream().map(this::toDeliveryResponse).toList());
    }

    @GetMapping("/admin/pengiriman/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> detailAdmin(@PathVariable("id") UUID id) {
        Delivery delivery = deliveryService.getById(id);
        return ResponseEntity.ok(toDeliveryResponse(delivery));
    }

    @PostMapping("/admin/pengiriman/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> approveAdmin(@PathVariable("id") UUID id) {
        Delivery delivery = deliveryService.approveByAdmin(id);
        return ResponseEntity.ok(toDeliveryResponse(delivery));
    }

    @PostMapping("/admin/pengiriman/{id}/partial-reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> partialRejectAdmin(
            @PathVariable("id") UUID id,
            @Valid @RequestBody PartialRejectRequest request) {
        Delivery delivery = deliveryService.partialRejectByAdmin(id, request.recognizedKg(), request.reason());
        return ResponseEntity.ok(toDeliveryResponse(delivery));
    }

    @PostMapping("/admin/pengiriman/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
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
