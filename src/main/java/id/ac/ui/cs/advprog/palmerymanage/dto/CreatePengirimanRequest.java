package id.ac.ui.cs.advprog.palmerymanage.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreatePengirimanRequest(
        @NotNull(message = "Supir harus dipilih")
        String supirId,
        @NotEmpty(message = "Minimal satu hasil panen harus dipilih")
        List<String> panenIds
) {
}

