package id.ac.ui.cs.advprog.palmerymanage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class WorkerAssignmentResponseDto {

    private UUID workerId;
    private UUID mandorId;
    private LocalDateTime assignedAt;
    private LocalDateTime updatedAt;
}
