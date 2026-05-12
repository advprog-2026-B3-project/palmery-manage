package id.ac.ui.cs.advprog.palmerymanage.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "harvest_photos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HarvestPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "harvest_result_id", nullable = false)
    private HarvestResult harvestResult;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String url; // Menyimpan URL gambar dari Rustfs

    @Column(nullable = false)
    private String filename;

    @Column(name = "size_bytes")
    private Integer sizeBytes;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
}