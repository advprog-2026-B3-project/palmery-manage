package id.ac.ui.cs.advprog.palmerymanage.controller;

import id.ac.ui.cs.advprog.palmerymanage.dto.AssignWorkerRequestDto;
import id.ac.ui.cs.advprog.palmerymanage.dto.WorkerAssignmentDto;
import id.ac.ui.cs.advprog.palmerymanage.service.WorkerAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WorkerAssignmentController {

    private final WorkerAssignmentService workerAssignmentService;

    @PostMapping("/api/admin/worker-assignments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkerAssignmentDto> assignWorker(@Valid @RequestBody AssignWorkerRequestDto request) {
        return ResponseEntity.ok(
                workerAssignmentService.assignWorker(request.getWorkerId(), request.getMandorId())
        );
    }

    @DeleteMapping("/api/admin/worker-assignments/{workerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unassignWorker(@PathVariable UUID workerId) {
        workerAssignmentService.unassignWorker(workerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/admin/worker-assignments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WorkerAssignmentDto>> getAllAssignments() {
        return ResponseEntity.ok(workerAssignmentService.getAllAssignments());
    }

    @GetMapping("/api/admin/worker-assignments/by-mandor/{mandorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANDOR')")
    public ResponseEntity<List<WorkerAssignmentDto>> getByMandor(@PathVariable UUID mandorId) {
        return ResponseEntity.ok(workerAssignmentService.getWorkersForMandor(mandorId));
    }

    @GetMapping("/api/admin/worker-assignments/by-worker/{workerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANDOR', 'BURUH')")
    public ResponseEntity<WorkerAssignmentDto> getByWorker(@PathVariable UUID workerId) {
        return workerAssignmentService.getAssignmentForWorker(workerId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Endpoint Mandor untuk melihat seluruh Buruh yang berada di bawah pengawasannya.
     */
    @GetMapping("/api/mandor/workers")
    @PreAuthorize("hasRole('MANDOR')")
    public ResponseEntity<List<WorkerAssignmentDto>> getMyWorkers(
            @RequestHeader("X-User-Id") UUID mandorId) {
        return ResponseEntity.ok(workerAssignmentService.getWorkersForMandor(mandorId));
    }
}
