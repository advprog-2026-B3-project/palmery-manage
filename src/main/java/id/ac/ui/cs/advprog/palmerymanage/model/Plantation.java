package id.ac.ui.cs.advprog.palmerymanage.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "plantation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plantation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "area_ha", nullable = false)
    private Double areaHa;

    // Top-Left
    @Column(name = "coord_tl_lat", nullable = false)
    private Double coordTlLat;

    @Column(name = "coord_tl_lon", nullable = false)
    private Double coordTlLon;

    // Top-Right
    @Column(name = "coord_tr_lat", nullable = false)
    private Double coordTrLat;

    @Column(name = "coord_tr_lon", nullable = false)
    private Double coordTrLon;

    // Bottom-Right
    @Column(name = "coord_br_lat", nullable = false)
    private Double coordBrLat;

    @Column(name = "coord_br_lon", nullable = false)
    private Double coordBrLon;

    // Bottom-Left
    @Column(name = "coord_bl_lat", nullable = false)
    private Double coordBlLat;

    @Column(name = "coord_bl_lon", nullable = false)
    private Double coordBlLon;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
