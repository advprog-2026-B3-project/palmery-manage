package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.dto.WorkerAssignmentDto;
import id.ac.ui.cs.advprog.palmerymanage.exception.BadRequestException;
import id.ac.ui.cs.advprog.palmerymanage.model.MandorWorkerAssignment;
import id.ac.ui.cs.advprog.palmerymanage.repository.MandorWorkerAssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerAssignmentService {

    private final MandorWorkerAssignmentRepository repository;

    @Transactional
    public WorkerAssignmentDto assignWorker(UUID workerId, UUID mandorId) {
        if (workerId == null || mandorId == null) {
            throw new BadRequestException("Worker ID dan Mandor ID tidak boleh kosong");
        }
        if (workerId.equals(mandorId)) {
            throw new BadRequestException("Worker ID dan Mandor ID tidak boleh sama");
        }

        Optional<MandorWorkerAssignment> existing = repository.findByWorkerId(workerId);

        MandorWorkerAssignment assignment = existing.map(current -> {
            if (current.getMandorId().equals(mandorId)) {
                throw new BadRequestException("Buruh sudah berada di bawah Mandor tersebut");
            }
            log.info("Reassigning worker {} from mandor {} to mandor {}",
                    workerId, current.getMandorId(), mandorId);
            current.setMandorId(mandorId);
            return current;
        }).orElseGet(() -> {
            log.info("Assigning worker {} to mandor {}", workerId, mandorId);
            return MandorWorkerAssignment.builder()
                    .workerId(workerId)
                    .mandorId(mandorId)
                    .build();
        });

        MandorWorkerAssignment saved = repository.save(assignment);
        return toDto(saved);
    }

    @Transactional
    public void unassignWorker(UUID workerId) {
        if (workerId == null) {
            throw new BadRequestException("Worker ID tidak boleh kosong");
        }
        MandorWorkerAssignment current = repository.findByWorkerId(workerId)
                .orElseThrow(() -> new BadRequestException("Buruh belum ditugaskan ke Mandor manapun"));
        repository.delete(current);
        log.info("Unassigned worker {} from mandor {}", workerId, current.getMandorId());
    }

    @Transactional(readOnly = true)
    public List<WorkerAssignmentDto> getWorkersForMandor(UUID mandorId) {
        return repository.findByMandorId(mandorId).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<WorkerAssignmentDto> getAllAssignments() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Optional<WorkerAssignmentDto> getAssignmentForWorker(UUID workerId) {
        return repository.findByWorkerId(workerId).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public boolean isWorkerUnderMandor(UUID mandorId, UUID workerId) {
        if (mandorId == null || workerId == null) return false;
        return repository.existsByWorkerIdAndMandorId(workerId, mandorId);
    }

    private WorkerAssignmentDto toDto(MandorWorkerAssignment a) {
        return WorkerAssignmentDto.builder()
                .workerId(a.getWorkerId())
                .mandorId(a.getMandorId())
                .assignedAt(a.getAssignedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
