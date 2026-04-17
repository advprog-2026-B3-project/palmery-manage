package id.ac.ui.cs.advprog.palmerymanage.controller;

import id.ac.ui.cs.advprog.palmerymanage.dto.HarvestRequestDto;
import id.ac.ui.cs.advprog.palmerymanage.dto.ValidationRequestDto;
import id.ac.ui.cs.advprog.palmerymanage.model.HarvestResult;
import id.ac.ui.cs.advprog.palmerymanage.service.HarvestService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/harvests")
public class HarvestController {

    private final HarvestService harvestService;

    public HarvestController(HarvestService harvestService) {
        this.harvestService = harvestService;
    }

    //Endpoint Buruh Submit Panen
    @PostMapping
    public ResponseEntity<?> submitHarvest(
            @RequestHeader(value = "X-User-Id", required = true) UUID workerId,
            @RequestHeader(value = "X-User-Role", required = true) String role,
            @RequestBody HarvestRequestDto request) {
        try {
            if (!"BURUH".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Akses ditolak: Hanya Buruh yang bisa mencatat hasil panen.");
            }
            HarvestResult result = harvestService.submitHarvest(workerId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    //Endpoint untuk Mandor  validasi Panen
    @PatchMapping("/{id}/validate")
    public ResponseEntity<?> validateHarvest(
            @RequestHeader(value = "X-User-Id", required = true) UUID mandorId,
            @RequestHeader(value = "X-User-Role", required = true) String role,
            @PathVariable("id") UUID id,
            @RequestBody ValidationRequestDto request) {
        try {
            if (!"MANDOR".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Akses ditolak: Hanya Mandor yang bisa memvalidasi panen.");
            }
            HarvestResult result = harvestService.validateHarvest(mandorId, id, request);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. Endpoint Riwayat Buruh Pribadi
    @GetMapping("/me")
    public ResponseEntity<?> getMyHarvestHistory(
            @RequestHeader(value = "X-User-Id", required = true) UUID workerId,
            @RequestHeader(value = "X-User-Role", required = true) String role,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) String status) {
        try {
            if (!"BURUH".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Akses ditolak: Hanya Buruh yang bisa melihat riwayat pribadinya.");
            }
            List<HarvestResult> history = harvestService.getBuruhHistory(workerId, start, end, status);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. Endpoint Daftar Panen untuk Mandor (Bisa filter tanggal & workerId)
    @GetMapping
    public ResponseEntity<?> getAllHarvestsForMandor(
            @RequestHeader(value = "X-User-Role", required = true) String role,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) UUID workerId) {
        try {
            if (!"MANDOR".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Akses ditolak: Hanya Mandor yang bisa melihat semua data panen.");
            }
            List<HarvestResult> harvests = harvestService.getMandorHistory(date, workerId);
            return ResponseEntity.ok(harvests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //Endpoint 1 data panen
    @GetMapping("/{id}")
    public ResponseEntity<?> getHarvestById(@PathVariable("id") UUID id) {
        try {
            HarvestResult harvest = harvestService.getHarvestById(id);
            return ResponseEntity.ok(harvest);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    //endpoint mandor untuk melihat 1 buruh
    @GetMapping("/worker/{workerId}")
    public ResponseEntity<?> getHarvestsByWorkerId(
            @RequestHeader("X-User-Role") String role,
            @PathVariable("workerId") UUID workerId) {
        try {
            if (!"MANDOR".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Akses ditolak.");
            }
            List<HarvestResult> harvests = harvestService.getHarvestsByWorkerId(workerId);
            return ResponseEntity.ok(harvests);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}