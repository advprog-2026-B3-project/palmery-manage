package id.ac.ui.cs.advprog.palmerymanage.event;

import java.time.Instant;
import java.util.UUID;

public record HarvestApprovedEvent(
        UUID harvestId,
        String workerId,
        String mandorId,
        String plantationId,
        float kgHarvested,
        Instant timestamp
) {
}