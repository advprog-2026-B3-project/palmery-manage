package id.ac.ui.cs.advprog.palmerymanage.controller;

import id.ac.ui.cs.advprog.palmerymanage.dto.CreatePengirimanRequest;
import id.ac.ui.cs.advprog.palmerymanage.dto.PartialRejectRequest;
import id.ac.ui.cs.advprog.palmerymanage.dto.RejectRequest;
import id.ac.ui.cs.advprog.palmerymanage.dto.UpdateStatusRequest;
import id.ac.ui.cs.advprog.palmerymanage.exception.BadRequestException;
import id.ac.ui.cs.advprog.palmerymanage.model.Pengiriman;
import id.ac.ui.cs.advprog.palmerymanage.model.PengirimanStatus;
import id.ac.ui.cs.advprog.palmerymanage.pengiriman.PengirimanResponseMapper;
import id.ac.ui.cs.advprog.palmerymanage.service.PengirimanService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class PengirimanController {

    private final PengirimanService pengirimanService;
    private final PengirimanResponseMapper pengirimanResponseMapper;

    public PengirimanController(PengirimanService pengirimanService,
                                PengirimanResponseMapper pengirimanResponseMapper) {
        this.pengirimanService = pengirimanService;
        this.pengirimanResponseMapper = pengirimanResponseMapper;
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

    private String requireUserId(Authentication authentication, String headerFallback, String label) {
        String userId = resolveUserId(authentication, headerFallback, "");
        if (userId.isBlank()) {
            throw new BadRequestException(label + " tidak ditemukan");
        }
        return userId;
    }

    @GetMapping("/mandor/drivers")
    public ResponseEntity<List<Map<String, Object>>> driversForMandor(
            @RequestHeader(value = "X-User-Id", required = false) String mandorIdHeader,
            Authentication authentication,
            @RequestParam(value = "search", required = false, defaultValue = "") String search) {
        String mandorId = resolveUserId(authentication, mandorIdHeader, "");
        if (mandorId.isBlank()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(pengirimanService.listSupirOnKebunMandor(mandorId, search));
    }

    @GetMapping("/mandor/panen/siap-angkut")
    public ResponseEntity<List<Map<String, Object>>> panenSiapAngkut(
            @RequestHeader(value = "X-User-Id", required = false) String mandorIdHeader,
            Authentication authentication) {
        String mandorId = requireUserId(authentication, mandorIdHeader, "Mandor");
        return ResponseEntity.ok(pengirimanService.listPanenSiapAngkutForMandor(mandorId));
    }

    @PostMapping("/mandor/pengiriman")
    public ResponseEntity<Map<String, Object>> createPengiriman(
            @RequestHeader(value = "X-User-Id", required = false) String mandorIdHeader,
            Authentication authentication,
            @Valid @RequestBody CreatePengirimanRequest request) {
        String mandorId = requireUserId(authentication, mandorIdHeader, "Mandor");
        Pengiriman pengiriman = pengirimanService.createPengiriman(mandorId, request);
        return ResponseEntity.ok(pengirimanResponseMapper.toResponse(pengiriman));
    }

    @GetMapping("/mandor/pengiriman/aktif")
    public ResponseEntity<List<Map<String, Object>>> pengirimanAktifMandor(
            @RequestHeader(value = "X-User-Id", required = false) String mandorIdHeader,
            Authentication authentication) {
        String mandorId = requireUserId(authentication, mandorIdHeader, "Mandor");
        List<Pengiriman> list = pengirimanService.pengirimanAktifMandor(mandorId);
        return ResponseEntity.ok(list.stream().map(pengirimanResponseMapper::toResponse).toList());
    }

    @GetMapping("/mandor/pengiriman/{id}")
    public ResponseEntity<Map<String, Object>> detailMandor(
            @RequestHeader(value = "X-User-Id", required = false) String mandorIdHeader,
            Authentication authentication,
            @PathVariable UUID id) {
        String mandorId = requireUserId(authentication, mandorIdHeader, "Mandor");
        Pengiriman pengiriman = pengirimanService.getByIdForMandor(mandorId, id);
        return ResponseEntity.ok(pengirimanResponseMapper.toResponse(pengiriman));
    }

    @GetMapping("/mandor/supir/{supirId}/pengiriman")
    public ResponseEntity<List<Map<String, Object>>> pengirimanBySupirForMandor(
            @RequestHeader(value = "X-User-Id", required = false) String mandorIdHeader,
            Authentication authentication,
            @PathVariable String supirId,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to) {
        String mandorId = requireUserId(authentication, mandorIdHeader, "Mandor");
        LocalDate fromDate = from != null && !from.isBlank()
                ? LocalDate.parse(from)
                : LocalDate.of(2000, 1, 1);
        LocalDate toDate = to != null && !to.isBlank()
                ? LocalDate.parse(to)
                : LocalDate.now();
        List<Pengiriman> list = pengirimanService.pengirimanBySupirForMandor(
                mandorId, supirId, fromDate, toDate);
        return ResponseEntity.ok(list.stream().map(pengirimanResponseMapper::toResponse).toList());
    }

    @PostMapping("/mandor/pengiriman/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveMandor(
            @RequestHeader(value = "X-User-Id", required = false) String mandorIdHeader,
            Authentication authentication,
            @PathVariable UUID id) {
        String mandorId = requireUserId(authentication, mandorIdHeader, "Mandor");
        Pengiriman pengiriman = pengirimanService.approveByMandor(mandorId, id);
        return ResponseEntity.ok(pengirimanResponseMapper.toResponse(pengiriman));
    }

    @PostMapping("/mandor/pengiriman/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectMandor(
            @RequestHeader(value = "X-User-Id", required = false) String mandorIdHeader,
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody RejectRequest request) {
        String mandorId = requireUserId(authentication, mandorIdHeader, "Mandor");
        Pengiriman pengiriman = pengirimanService.rejectByMandor(mandorId, id, request.reason());
        return ResponseEntity.ok(pengirimanResponseMapper.toResponse(pengiriman));
    }

    @GetMapping("/supir/pengiriman/aktif")
    public ResponseEntity<List<Map<String, Object>>> pengirimanAktifSupir(
            @RequestHeader(value = "X-User-Id", required = false) String supirIdHeader,
            Authentication authentication) {
        String supirId = requireUserId(authentication, supirIdHeader, "Supir");
        List<Pengiriman> list = pengirimanService.pengirimanAktifSupir(supirId);
        return ResponseEntity.ok(list.stream().map(pengirimanResponseMapper::toResponse).toList());
    }

    @GetMapping("/supir/pengiriman/{id}")
    public ResponseEntity<Map<String, Object>> detailSupir(
            @RequestHeader(value = "X-User-Id", required = false) String supirIdHeader,
            Authentication authentication,
            @PathVariable UUID id) {
        String supirId = requireUserId(authentication, supirIdHeader, "Supir");
        Pengiriman pengiriman = pengirimanService.getByIdForSupir(supirId, id);
        return ResponseEntity.ok(pengirimanResponseMapper.toResponse(pengiriman));
    }

    @GetMapping("/supir/pengiriman/riwayat")
    public ResponseEntity<List<Map<String, Object>>> riwayatSupir(
            @RequestHeader(value = "X-User-Id", required = false) String supirIdHeader,
            Authentication authentication,
            @RequestParam("from") String from,
            @RequestParam("to") String to) {
        String supirId = requireUserId(authentication, supirIdHeader, "Supir");
        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate = LocalDate.parse(to);
        List<Pengiriman> list = pengirimanService.riwayatSupir(supirId, fromDate, toDate);
        return ResponseEntity.ok(list.stream().map(pengirimanResponseMapper::toResponse).toList());
    }

    @PatchMapping("/supir/pengiriman/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatusSupir(
            @RequestHeader(value = "X-User-Id", required = false) String supirIdHeader,
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request) {
        String supirId = requireUserId(authentication, supirIdHeader, "Supir");
        PengirimanStatus target = PengirimanStatus.valueOf(request.status());
        Pengiriman pengiriman = pengirimanService.updateStatusSupir(supirId, id, target);
        return ResponseEntity.ok(pengirimanResponseMapper.toResponse(pengiriman));
    }

    @GetMapping("/admin/pengiriman/pending")
    public ResponseEntity<List<Map<String, Object>>> pendingAdmin(
            @RequestParam(value = "mandor", required = false) String mandorSearch,
            @RequestParam(value = "date", required = false) String date) {
        LocalDate parsedDate = date != null && !date.isBlank() ? LocalDate.parse(date) : null;
        List<Pengiriman> list = pengirimanService.pendingAdmin(mandorSearch, parsedDate);
        return ResponseEntity.ok(list.stream().map(pengirimanResponseMapper::toResponse).toList());
    }

    @GetMapping("/admin/pengiriman/{id}")
    public ResponseEntity<Map<String, Object>> detailAdmin(@PathVariable UUID id) {
        Pengiriman pengiriman = pengirimanService.getById(id);
        return ResponseEntity.ok(pengirimanResponseMapper.toResponse(pengiriman));
    }

    @PostMapping("/admin/pengiriman/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveAdmin(@PathVariable UUID id) {
        Pengiriman pengiriman = pengirimanService.approveByAdmin(id);
        return ResponseEntity.ok(pengirimanResponseMapper.toResponse(pengiriman));
    }

    @PostMapping("/admin/pengiriman/{id}/partial-reject")
    public ResponseEntity<Map<String, Object>> partialRejectAdmin(
            @PathVariable UUID id,
            @Valid @RequestBody PartialRejectRequest request) {
        Pengiriman pengiriman = pengirimanService.partialRejectByAdmin(
                id, request.recognizedKg(), request.reason());
        return ResponseEntity.ok(pengirimanResponseMapper.toResponse(pengiriman));
    }

    @PostMapping("/admin/pengiriman/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectAdmin(
            @PathVariable UUID id,
            @Valid @RequestBody RejectRequest request) {
        Pengiriman pengiriman = pengirimanService.rejectByAdmin(id, request.reason());
        return ResponseEntity.ok(pengirimanResponseMapper.toResponse(pengiriman));
    }
}
