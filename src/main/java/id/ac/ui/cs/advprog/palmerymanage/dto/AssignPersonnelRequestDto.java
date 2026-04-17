package id.ac.ui.cs.advprog.palmerymanage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignPersonnelRequestDto {

    @NotNull(message = "Personnel ID tidak boleh kosong")
    private UUID personnelId;
}
