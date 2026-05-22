package id.ac.ui.cs.advprog.palmerymanage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/**
 * Permintaan untuk memindahkan personel (Mandor / Supir) dari satu kebun ke kebun lain
 * dalam satu transaksi atomik.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferPersonnelRequestDto {

    @NotNull(message = "Personnel ID tidak boleh kosong")
    private UUID personnelId;

    @NotNull(message = "Kebun tujuan tidak boleh kosong")
    private UUID toPlantationId;
}
