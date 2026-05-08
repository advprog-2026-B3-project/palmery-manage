package id.ac.ui.cs.advprog.palmerymanage.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PengirimanTibaEvent(
        UUID pengirimanId,
        String supirId,
        String mandorId,
        int totalKg,
        List<String> panenIds,
        Instant timestamp
) {
}

