package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.dto.WorkerAssignmentResponseDto;
import id.ac.ui.cs.advprog.palmerymanage.exception.BadRequestException;
import id.ac.ui.cs.advprog.palmerymanage.model.PlantationAssignment;
import id.ac.ui.cs.advprog.palmerymanage.model.WorkerAssignment;
import id.ac.ui.cs.advprog.palmerymanage.repository.PlantationAssignmentRepository;
import id.ac.ui.cs.advprog.palmerymanage.repository.WorkerAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkerAssignmentServiceImpl implements WorkerAssignmentService {

    private final WorkerAssignmentRepository workerAssignmentRepository;
    private final PlantationAssignmentRepository plantationAssignmentRepository;

    @Override
    @Transactional
    public WorkerAssignmentResponseDto assignWorker(UUID workerId, UUID mandorId) {
        ensureMandorHasPlantation(mandorId);

        WorkerAssignment assignment = workerAssignmentRepository.findById(workerId)
                .orElse(WorkerAssignment.builder().workerId(workerId).build());
        assignment.setMandorId(mandorId);

        return toResponse(workerAssignmentRepository.save(assignment));
    }

    @Override
    @Transactional
    public void unassignWorker(UUID workerId) {
        WorkerAssignment assignment = workerAssignmentRepository.findById(workerId)
                .orElseThrow(() -> new IllegalArgumentException("Penempatan Buruh tidak ditemukan"));
        workerAssignmentRepository.delete(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkerAssignmentResponseDto> getAllAssignments() {
        return workerAssignmentRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkerAssignmentResponseDto> getWorkersForMandor(UUID mandorId) {
        return workerAssignmentRepository.findByMandorId(mandorId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkerAssignmentResponseDto getAssignmentByWorker(UUID workerId) {
        return workerAssignmentRepository.findById(workerId)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Penempatan Buruh tidak ditemukan"));
    }

    private void ensureMandorHasPlantation(UUID mandorId) {
        boolean assigned = !plantationAssignmentRepository
                .findByPersonnelIdAndRole(mandorId, PlantationAssignment.PersonnelRole.MANDOR)
                .isEmpty();
        if (!assigned) {
            throw new BadRequestException("Mandor belum ditugaskan ke kebun");
        }
    }

    private WorkerAssignmentResponseDto toResponse(WorkerAssignment assignment) {
        return WorkerAssignmentResponseDto.builder()
                .workerId(assignment.getWorkerId())
                .mandorId(assignment.getMandorId())
                .assignedAt(assignment.getAssignedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }
}
