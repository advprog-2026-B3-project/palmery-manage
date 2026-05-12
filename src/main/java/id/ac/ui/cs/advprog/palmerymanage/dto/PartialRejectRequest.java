package id.ac.ui.cs.advprog.palmerymanage.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PartialRejectRequest(
        @NotNull(message = "Berat yang diakui wajib diisi")
        @Min(value = 1, message = "Berat yang diakui harus lebih dari 0")
        Integer recognizedKg,
        @NotBlank(message = "Alasan wajib diisi")
        String reason
) {
}

