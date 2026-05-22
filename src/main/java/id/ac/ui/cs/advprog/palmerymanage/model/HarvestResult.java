package id.ac.ui.cs.advprog.palmerymanage.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "harvest_results", uniqueConstraints = {
        @UniqueConstraint(name = "uq_harvest_worker_per_day", columnNames = {"worker_id", "harvest_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HarvestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "worker_id", nullable = false)
    private UUID workerId;

    @Column(name = "mandor_id", nullable = false)
    private UUID mandorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plantation_id", nullable = false)
    private Plantation plantation;

    @Column(name = "harvest_date", nullable = false)
    private LocalDate harvestDate;

    @Column(name = "kg_harvested", nullable = false)
    private Float kgHarvested;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String notes;

    @Builder.Default
    @Column(name = "ready_for_delivery", nullable = false)
    private Boolean readyForDelivery = false;

    @Column(nullable = false, length = 20)
    private String status; // PENDING, APPROVED, REJECTED

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "validated_at")
    private LocalDateTime validatedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "harvestResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HarvestPhoto> photos = new ArrayList<>();

    public UUID getPlantationId() {
        return plantation != null ? plantation.getId() : null;
    }
}