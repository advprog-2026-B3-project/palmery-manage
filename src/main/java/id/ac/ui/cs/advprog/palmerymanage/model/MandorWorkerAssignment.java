package id.ac.ui.cs.advprog.palmerymanage.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Pemetaan Buruh ke Mandor.
 *
 * Setiap Buruh hanya bisa berada di bawah satu Mandor pada satu waktu, sehingga
 * {@code worker_id} dijadikan primary key. Untuk reassignment, baris yang sama
 * di-update kolom {@code mandor_id} nya (bukan dihapus dan dibuat ulang).
 */
@Entity
@Table(name = "mandor_worker_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MandorWorkerAssignment {

    @Id
    @Column(name = "worker_id", nullable = false, updatable = false)
    private UUID workerId;

    @Column(name = "mandor_id", nullable = false)
    private UUID mandorId;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
