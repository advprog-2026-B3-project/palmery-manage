package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.model.AdminApprovalStatus;
import id.ac.ui.cs.advprog.palmerymanage.model.ApprovalStatus;
import id.ac.ui.cs.advprog.palmerymanage.model.Pengiriman;
import id.ac.ui.cs.advprog.palmerymanage.model.PengirimanStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PengirimanMonitoringService {

    private final MeterRegistry meterRegistry;

    public void recordOperation(String operation, long startedAtNanos, boolean success) {
        Timer.builder("palmery.pengiriman.operation.duration")
                .description("Duration of pengiriman service operations")
                .tag("operation", operation)
                .tag("result", success ? "success" : "error")
                .register(meterRegistry)
                .record(System.nanoTime() - startedAtNanos, TimeUnit.NANOSECONDS);
    }

    public void recordCreated(Pengiriman pengiriman) {
        Counter.builder("palmery.pengiriman.created")
                .description("Number of pengiriman records created")
                .tag("status", tagValue(pengiriman.getStatus()))
                .register(meterRegistry)
                .increment();

        DistributionSummary.builder("palmery.pengiriman.total_kg")
                .description("Total kg per created pengiriman")
                .baseUnit("kg")
                .register(meterRegistry)
                .record(pengiriman.getTotalKg());
    }

    public void recordStatusTransition(PengirimanStatus from, PengirimanStatus to) {
        Counter.builder("palmery.pengiriman.status.transition")
                .description("Supir pengiriman status transitions")
                .tag("from", tagValue(from))
                .tag("to", tagValue(to))
                .register(meterRegistry)
                .increment();
    }

    public void recordMandorDecision(ApprovalStatus status) {
        Counter.builder("palmery.pengiriman.mandor.decision")
                .description("Mandor approval decisions for pengiriman")
                .tag("status", tagValue(status))
                .register(meterRegistry)
                .increment();
    }

    public void recordAdminDecision(AdminApprovalStatus status) {
        Counter.builder("palmery.pengiriman.admin.decision")
                .description("Admin approval decisions for pengiriman")
                .tag("status", tagValue(status))
                .register(meterRegistry)
                .increment();
    }

    private String tagValue(Enum<?> value) {
        return value == null ? "UNKNOWN" : value.name();
    }
}
