package id.ac.ui.cs.advprog.palmerymanage.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "deliveries")
@Getter
@Setter
@NoArgsConstructor
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "supir_id", nullable = false)
    private String supirId;

    @Column(name = "mandor_id", nullable = false)
    private String mandorId;

    // Untuk saat ini kebun_id diisi dengan plantationId (UUID string) dari panen pertama / mandor.
    @Column(name = "kebun_id", nullable = false)
    private String kebunId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private DeliveryStatus status = DeliveryStatus.MEMUAT;

    @Column(name = "total_kg", nullable = false)
    private int totalKg;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "delivery_panen_ids", joinColumns = @JoinColumn(name = "delivery_id"))
    @Column(name = "panen_id", nullable = false)
    private List<String> panenIds = new ArrayList<>();

    @Column(name = "rejected_reason", columnDefinition = "TEXT")
    private String rejectedReason;

    @Column(name = "recognized_kg")
    private Integer recognizedKg;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

