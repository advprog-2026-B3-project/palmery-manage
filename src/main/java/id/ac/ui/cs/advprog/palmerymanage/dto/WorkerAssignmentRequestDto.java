package id.ac.ui.cs.advprog.palmerymanage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class WorkerAssignmentRequestDto {

    @NotNull(message = "Worker ID tidak boleh kosong")
    private UUID workerId;

    @NotNull(message = "Mandor ID tidak boleh kosong")
    private UUID mandorId;
}
