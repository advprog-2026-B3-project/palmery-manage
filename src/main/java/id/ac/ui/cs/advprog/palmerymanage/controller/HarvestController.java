package id.ac.ui.cs.advprog.palmerymanage.controller;

import id.ac.ui.cs.advprog.palmerymanage.dto.HarvestRequestDto;
import id.ac.ui.cs.advprog.palmerymanage.dto.HarvestResponseDto;
import id.ac.ui.cs.advprog.palmerymanage.dto.ValidationRequestDto;
import id.ac.ui.cs.advprog.palmerymanage.model.HarvestPhoto;
import id.ac.ui.cs.advprog.palmerymanage.model.HarvestResult;
import id.ac.ui.cs.advprog.palmerymanage.service.HarvestService;
import org.hibernate.Hibernate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/harvests")
public class HarvestController {

    private final HarvestService harvestService;

    public HarvestController(HarvestService harvestService) {
        this.harvestService = harvestService;
    }

    private HarvestResponseDto mapToResponseDto(HarvestResult result) {
        if (result == null) return null;
        return HarvestResponseDto.builder()
                .id(result.getId())
                .workerId(result.getWorkerId())
                .mandorId(result.getMandorId())
                .plantationId(result.getPlantationId())
                .harvestDate(result.getHarvestDate())
                .kgHarvested(result.getKgHarvested())
                .notes(result.getNotes())
                .readyForDelivery(result.getReadyForDelivery())
                .status(result.getStatus())
                .rejectionReason(result.getRejectionReason())
                .validatedAt(result.getValidatedAt())
                .createdAt(result.getCreatedAt())
                .photos(mapPhotoDtos(result))
                .build();
    }

    private List<HarvestResponseDto.PhotoDto> mapPhotoDtos(HarvestResult result) {
        List<HarvestPhoto> photos = result.getPhotos();
        if (photos == null || !Hibernate.isInitialized(photos)) {
            return List.of();
        }
        return photos.stream().map(photo ->
                HarvestResponseDto.PhotoDto.builder()
                        .id(photo.getId())
                        .url(photo.getUrl())
                        .filename(photo.getFilename())
                        .sizeBytes(photo.getSizeBytes())
                        .uploadedAt(photo.getUploadedAt())
                        .build()
        ).collect(Collectors.toList());
    }

    //Endpoint Buruh Submit Panen
    @PostMapping
    public ResponseEntity<?> submitHarvest(
            Authentication authentication,
            @RequestHeader(value = "X-User-Id", required = false) UUID workerIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestBody HarvestRequestDto request) {
        try {
            UUID workerId = resolveRequiredUserId(authentication, workerIdHeader, "Buruh");
            String role = resolveRole(authentication, roleHeader);
            if (!"BURUH".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Akses ditolak: Hanya Buruh yang bisa mencatat hasil panen.");
            }
            HarvestResult result = harvestService.submitHarvest(workerId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponseDto(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    //Endpoint untuk Mandor  validasi Panen
    @PatchMapping("/{id}/validate")
    public ResponseEntity<?> validateHarvest(
            Authentication authentication,
            @RequestHeader(value = "X-User-Id", required = false) UUID mandorIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @PathVariable("id") UUID id,
            @RequestBody ValidationRequestDto request) {
        try {
            UUID mandorId = resolveRequiredUserId(authentication, mandorIdHeader, "Mandor");
            String role = resolveRole(authentication, roleHeader);
            if (!"MANDOR".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Akses ditolak: Hanya Mandor yang bisa memvalidasi panen.");
            }
            HarvestResult result = harvestService.validateHarvest(mandorId, id, request);
            return ResponseEntity.ok(mapToResponseDto(result));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. Endpoint Riwayat Buruh Pribadi
    @GetMapping("/me")
    public ResponseEntity<?> getMyHarvestHistory(
            Authentication authentication,
            @RequestHeader(value = "X-User-Id", required = false) UUID workerIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) String status) {
        try {
            UUID workerId = resolveRequiredUserId(authentication, workerIdHeader, "Buruh");
            String role = resolveRole(authentication, roleHeader);

            if (!"BURUH".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Akses ditolak: Hanya Buruh yang bisa melihat riwayat pribadinya.");
            }
            List<HarvestResult> history = harvestService.getBuruhHistory(workerId, start, end, status);
            return ResponseEntity.ok(history.stream().map(this::mapToResponseDto).collect(Collectors.toList()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private UUID resolveUserId(Authentication authentication, UUID userIdHeader) {
        if (authentication != null && authentication.getName() != null && !authentication.getName().isBlank()) {
            try {
                return UUID.fromString(authentication.getName());
            } catch (IllegalArgumentException ignored) {
                // Fall through to the legacy header fallback below.
            }
        }
        return userIdHeader;
    }

    private UUID resolveRequiredUserId(Authentication authentication, UUID userIdHeader, String label) {
        UUID userId = resolveUserId(authentication, userIdHeader);
        if (userId == null) {
            throw new IllegalArgumentException(label + " ID tidak ditemukan");
        }
        return userId;
    }

    private String resolveRole(Authentication authentication, String roleHeader) {
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .filter(authority -> authority != null && authority.startsWith("ROLE_"))
                    .map(authority -> authority.substring("ROLE_".length()))
                    .findFirst()
                    .orElse(roleHeader);
        }
        return roleHeader;
    }

    // 4. Endpoint Daftar Panen untuk Mandor (Bisa filter tanggal & workerId)
    @GetMapping
    public ResponseEntity<?> getAllHarvestsForMandor(
            Authentication authentication,
            @RequestHeader(value = "X-User-Id", required = false) UUID mandorIdHeader,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) UUID workerId) {
        try {
            UUID mandorId = resolveRequiredUserId(authentication, mandorIdHeader, "Mandor");
            String role = resolveRole(authentication, roleHeader);
            if (!"MANDOR".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Akses ditolak: Hanya Mandor yang bisa melihat semua data panen.");
            }
            List<HarvestResult> harvests = harvestService.getMandorHistory(mandorId, date, workerId);
            return ResponseEntity.ok(harvests.stream().map(this::mapToResponseDto).collect(Collectors.toList()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //Endpoint 1 data panen
    @GetMapping("/{id}")
    public ResponseEntity<?> getHarvestById(@PathVariable("id") UUID id) {
        try {
            HarvestResult harvest = harvestService.getHarvestById(id);
            return ResponseEntity.ok(mapToResponseDto(harvest));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    //endpoint mandor untuk melihat 1 buruh
    @GetMapping("/worker/{workerId}")
    public ResponseEntity<?> getHarvestsByWorkerId(
            Authentication authentication,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader,
            @PathVariable("workerId") UUID workerId) {
        try {
            String role = resolveRole(authentication, roleHeader);
            if (!"MANDOR".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Akses ditolak.");
            }
            List<HarvestResult> harvests = harvestService.getHarvestsByWorkerId(workerId);
            return ResponseEntity.ok(harvests.stream().map(this::mapToResponseDto).collect(Collectors.toList()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
