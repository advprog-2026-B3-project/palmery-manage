package id.ac.ui.cs.advprog.palmerymanage.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateStatusRequest(
        @NotBlank(message = "Status wajib diisi")
        String status
) {
}

