package id.ac.ui.cs.advprog.palmerymanage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignWorkerRequestDto {

    @NotNull(message = "Worker ID tidak boleh kosong")
    private UUID workerId;

    @NotNull(message = "Mandor ID tidak boleh kosong")
    private UUID mandorId;
}
