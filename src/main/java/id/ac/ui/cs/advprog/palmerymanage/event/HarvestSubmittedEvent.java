package id.ac.ui.cs.advprog.palmerymanage.event;

import java.time.Instant;
import java.util.UUID;

public record HarvestSubmittedEvent(
        UUID harvestId,
        String workerId,
        String plantationId,
        float kgHarvested,
        Instant timestamp
) {}
