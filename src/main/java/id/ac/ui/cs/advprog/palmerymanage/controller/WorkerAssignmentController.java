package id.ac.ui.cs.advprog.palmerymanage.controller;

import id.ac.ui.cs.advprog.palmerymanage.dto.WorkerAssignmentRequestDto;
import id.ac.ui.cs.advprog.palmerymanage.dto.WorkerAssignmentResponseDto;
import id.ac.ui.cs.advprog.palmerymanage.service.WorkerAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WorkerAssignmentController {

    private final WorkerAssignmentService workerAssignmentService;

    @PostMapping("/api/admin/worker-assignments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WorkerAssignmentResponseDto> assignWorker(
            @Valid @RequestBody WorkerAssignmentRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workerAssignmentService.assignWorker(request.getWorkerId(), request.getMandorId()));
    }

    @DeleteMapping("/api/admin/worker-assignments/{workerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unassignWorker(@PathVariable UUID workerId) {
        workerAssignmentService.unassignWorker(workerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/admin/worker-assignments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WorkerAssignmentResponseDto>> getAllAssignments() {
        return ResponseEntity.ok(workerAssignmentService.getAllAssignments());
    }

    @GetMapping("/api/admin/worker-assignments/by-mandor/{mandorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WorkerAssignmentResponseDto>> getWorkersForMandor(@PathVariable UUID mandorId) {
        return ResponseEntity.ok(workerAssignmentService.getWorkersForMandor(mandorId));
    }

    @GetMapping("/api/admin/worker-assignments/by-worker/{workerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BURUH')")
    public ResponseEntity<WorkerAssignmentResponseDto> getAssignmentByWorker(@PathVariable UUID workerId) {
        try {
            return ResponseEntity.ok(workerAssignmentService.getAssignmentByWorker(workerId));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/api/mandor/workers")
    @PreAuthorize("hasRole('MANDOR')")
    public ResponseEntity<List<WorkerAssignmentResponseDto>> getMyWorkers(
            @RequestHeader("X-User-Id") UUID mandorId) {
        return ResponseEntity.ok(workerAssignmentService.getWorkersForMandor(mandorId));
    }
}
