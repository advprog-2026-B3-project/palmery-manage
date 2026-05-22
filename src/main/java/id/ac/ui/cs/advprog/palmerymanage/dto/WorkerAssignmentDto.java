package id.ac.ui.cs.advprog.palmerymanage.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkerAssignmentDto {
    private UUID workerId;
    private UUID mandorId;
    private LocalDateTime assignedAt;
    private LocalDateTime updatedAt;
}
