package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.dto.WorkerAssignmentResponseDto;

import java.util.List;
import java.util.UUID;

public interface WorkerAssignmentService {

    WorkerAssignmentResponseDto assignWorker(UUID workerId, UUID mandorId);

    void unassignWorker(UUID workerId);

    List<WorkerAssignmentResponseDto> getAllAssignments();

    List<WorkerAssignmentResponseDto> getWorkersForMandor(UUID mandorId);

    WorkerAssignmentResponseDto getAssignmentByWorker(UUID workerId);

    boolean isWorkerUnderMandor(UUID mandorId, UUID workerId);
}
