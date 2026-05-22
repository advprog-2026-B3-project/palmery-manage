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
@Table(name = "pengiriman")
@Getter
@Setter
@NoArgsConstructor
public class Pengiriman {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "supir_id", nullable = false)
    private String supirId;

    @Column(name = "mandor_id", nullable = false)
    private String mandorId;

    @Column(name = "kebun_id", nullable = false)
    private String kebunId;

    /** Delivery status: MEMUAT, MENGIRIM, TIBA_DI_TUJUAN */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PengirimanStatus status = PengirimanStatus.MEMUAT;

    /** Mandor approval after delivery arrives */
    @Enumerated(EnumType.STRING)
    @Column(name = "mandor_approval_status", nullable = false, length = 20)
    private ApprovalStatus mandorApprovalStatus = ApprovalStatus.PENDING;

    /** Admin approval after Mandor approves */
    @Enumerated(EnumType.STRING)
    @Column(name = "admin_approval_status", nullable = false, length = 30)
    private AdminApprovalStatus adminApprovalStatus = AdminApprovalStatus.PENDING;

    @Column(name = "total_kg", nullable = false)
    private int totalKg;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pengiriman_panen_ids", joinColumns = @JoinColumn(name = "pengiriman_id"))
    @Column(name = "panen_id", nullable = false)
    private List<String> panenIds = new ArrayList<>();

    @Column(name = "rejected_reason", columnDefinition = "TEXT")
    private String rejectedReason;

    /** Kg recognized/accepted by admin (for partial approval) */
    @Column(name = "recognized_kg")
    private Integer recognizedKg;

    /** Kg accepted by admin at factory validation */
    @Column(name = "accepted_kg_by_admin")
    private Integer acceptedKgByAdmin;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
