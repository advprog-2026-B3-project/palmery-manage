package id.ac.ui.cs.advprog.palmerymanage.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pengiriman")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "supir_id", nullable = false)
    private String supirId;

    @Column(name = "mandor_id", nullable = false)
    private String mandorId;

    @Column(name = "kebun_id", nullable = false)
    private String kebunId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @Column(name = "total_kg", nullable = false)
    private int totalKg;

    @ElementCollection
    @CollectionTable(
            name = "pengiriman_panen",
            joinColumns = @JoinColumn(name = "pengiriman_id")
    )
    @Column(name = "panen_id", nullable = false)
    private List<String> panenIds = new ArrayList<>();

    @Column(name = "rejected_reason")
    private String rejectedReason;

    @Column(name = "recognized_kg")
    private Integer recognizedKg;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = DeliveryStatus.MEMUAT;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSupirId() {
        return supirId;
    }

    public void setSupirId(String supirId) {
        this.supirId = supirId;
    }

    public String getMandorId() {
        return mandorId;
    }

    public void setMandorId(String mandorId) {
        this.mandorId = mandorId;
    }

    public String getKebunId() {
        return kebunId;
    }

    public void setKebunId(String kebunId) {
        this.kebunId = kebunId;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }

    public int getTotalKg() {
        return totalKg;
    }

    public void setTotalKg(int totalKg) {
        this.totalKg = totalKg;
    }

    public List<String> getPanenIds() {
        return panenIds;
    }

    public void setPanenIds(List<String> panenIds) {
        this.panenIds = panenIds;
    }

    public String getRejectedReason() {
        return rejectedReason;
    }

    public void setRejectedReason(String rejectedReason) {
        this.rejectedReason = rejectedReason;
    }

    public Integer getRecognizedKg() {
        return recognizedKg;
    }

    public void setRecognizedKg(Integer recognizedKg) {
        this.recognizedKg = recognizedKg;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

