package id.ac.ui.cs.advprog.palmerymanage.event;

import java.time.Instant;

public record PanenApprovedEvent(
        String panenId,
        String mandorId,
        String kebunId,
        int beratKg,
        Instant approvedAt
) {
}

