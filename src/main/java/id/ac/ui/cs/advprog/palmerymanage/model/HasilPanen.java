package id.ac.ui.cs.advprog.palmerymanage.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(name = "hasil_panen", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"buruh_id", "tanggal_panen"}) // Sesuai note: unik per hari
})
@Data
public class HasilPanen {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "buruh_id", nullable = false)
    private UUID buruhId; // Cross-service ref ke palmery-auth

    @Column(name = "kebun_id", nullable = false)
    private UUID kebunId;

    @Column(name = "tanggal_panen", nullable = false)
    private LocalDate tanggalPanen;

    @Column(name = "kg_dipanen", nullable = false, precision = 10, scale = 2)
    private BigDecimal kgDipanen;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String berita;
}
