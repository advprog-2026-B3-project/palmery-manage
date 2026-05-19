package id.ac.ui.cs.advprog.palmerymanage.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "plantation_assignments",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_plantation_personnel_role",
                columnNames = {"plantation_id", "personnel_id", "role"}
        ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantationAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "plantation_id", nullable = false)
    private UUID plantationId;

    @Column(name = "personnel_id", nullable = false)
    private UUID personnelId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PersonnelRole role;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    public enum PersonnelRole {
        MANDOR,
        SUPIR
    }
}
