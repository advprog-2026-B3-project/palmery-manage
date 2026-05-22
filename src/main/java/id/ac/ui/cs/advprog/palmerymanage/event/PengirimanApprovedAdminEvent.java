package id.ac.ui.cs.advprog.palmerymanage.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PengirimanApprovedAdminEvent(
        UUID pengirimanId,
        String supirId,
        String mandorId,
        int totalKg,
        int recognizedKg,
        List<String> panenIds,
        Instant timestamp
) {
}

