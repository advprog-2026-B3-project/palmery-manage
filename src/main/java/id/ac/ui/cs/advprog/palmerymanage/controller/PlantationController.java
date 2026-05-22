package id.ac.ui.cs.advprog.palmerymanage.controller;

import id.ac.ui.cs.advprog.palmerymanage.dto.*;
import id.ac.ui.cs.advprog.palmerymanage.service.PlantationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/kebun")
@RequiredArgsConstructor
public class PlantationController {

    private final PlantationService plantationService;

    /**
     * GET /kebun?name=...&code=...
     * Accessible by ADMIN and MANDOR (read-only)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANDOR')")
    public ResponseEntity<List<PlantationSummaryDto>> getAllPlantations(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code) {
        return ResponseEntity.ok(plantationService.getAllPlantations(name, code));
    }

    /**
     * GET /kebun/:id
     * Accessible by ADMIN and MANDOR (read-only)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANDOR')")
    public ResponseEntity<PlantationResponseDto> getPlantationById(@PathVariable UUID id) {
        return ResponseEntity.ok(plantationService.getPlantationById(id));
    }

    /**
     * POST /kebun
     * ADMIN only
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlantationResponseDto> createPlantation(
            @Valid @RequestBody PlantationRequestDto request) {
        PlantationResponseDto response = plantationService.createPlantation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /kebun/:id
     * ADMIN only
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlantationResponseDto> updatePlantation(
            @PathVariable UUID id,
            @Valid @RequestBody PlantationRequestDto request) {
        return ResponseEntity.ok(plantationService.updatePlantation(id, request));
    }

    /**
     * DELETE /kebun/:id
     * ADMIN only — guard: gagal jika masih ada Mandor terikat
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePlantation(@PathVariable UUID id) {
        plantationService.deletePlantation(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /kebun/:id/mandor
     * Assign Mandor ke Kebun — ADMIN only
     */
    @PostMapping("/{id}/mandor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignMandor(
            @PathVariable UUID id,
            @Valid @RequestBody AssignPersonnelRequestDto request) {
        plantationService.assignMandor(id, request.getPersonnelId());
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /kebun/:id/mandor
     * Unassign Mandor dari Kebun — ADMIN only
     */
    @DeleteMapping("/{id}/mandor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unassignMandor(
            @PathVariable UUID id,
            @Valid @RequestBody AssignPersonnelRequestDto request) {
        plantationService.unassignMandor(id, request.getPersonnelId());
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /kebun/:id/mandor/transfer
     * Transfer Mandor dari satu Kebun ke Kebun lain — ADMIN only
     */
    @PostMapping("/{id}/mandor/transfer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> transferMandor(
            @PathVariable UUID id,
            @Valid @RequestBody TransferPersonnelRequestDto request) {
        plantationService.transferMandor(id, request.getToPlantationId(), request.getPersonnelId());
        return ResponseEntity.ok().build();
    }

    /**
     * POST /kebun/:id/supir
     * Assign Supir ke Kebun — ADMIN only
     */
    @PostMapping("/{id}/supir")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignSupir(
            @PathVariable UUID id,
            @Valid @RequestBody AssignPersonnelRequestDto request) {
        plantationService.assignSupir(id, request.getPersonnelId());
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /kebun/:id/supir
     * Unassign Supir dari Kebun — ADMIN only
     */
    @DeleteMapping("/{id}/supir")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unassignSupir(
            @PathVariable UUID id,
            @Valid @RequestBody AssignPersonnelRequestDto request) {
        plantationService.unassignSupir(id, request.getPersonnelId());
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /kebun/:id/supir/transfer
     * Transfer Supir dari satu Kebun ke Kebun lain — ADMIN only
     */
    @PostMapping("/{id}/supir/transfer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> transferSupir(
            @PathVariable UUID id,
            @Valid @RequestBody TransferPersonnelRequestDto request) {
        plantationService.transferSupir(id, request.getToPlantationId(), request.getPersonnelId());
        return ResponseEntity.ok().build();
    }
}
