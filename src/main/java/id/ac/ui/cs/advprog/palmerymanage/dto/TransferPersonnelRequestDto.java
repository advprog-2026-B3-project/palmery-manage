package id.ac.ui.cs.advprog.palmerymanage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TransferPersonnelRequestDto {

    @NotNull(message = "Personnel ID tidak boleh kosong")
    private UUID personnelId;

    @NotNull(message = "Tujuan kebun tidak boleh kosong")
    private UUID toPlantationId;
}
