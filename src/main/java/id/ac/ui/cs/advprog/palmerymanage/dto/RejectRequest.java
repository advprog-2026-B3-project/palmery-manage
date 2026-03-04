package id.ac.ui.cs.advprog.palmerymanage.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectRequest(
        @NotBlank(message = "Alasan wajib diisi")
        String reason
) {
}

