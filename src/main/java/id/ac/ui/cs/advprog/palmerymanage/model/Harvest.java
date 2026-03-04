package id.ac.ui.cs.advprog.palmerymanage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "panen")
public class Harvest {

    @Id
    private String id;

    @Column(name = "mandor_id", nullable = false)
    private String mandorId;

    @Column(name = "kebun_id", nullable = false)
    private String kebunId;

    @Column(name = "berat_kg", nullable = false)
    private int beratKg;

    @Column(name = "ready_for_delivery", nullable = false)
    private boolean readyForDelivery;

    @Column(name = "status", nullable = false)
    private String status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public int getBeratKg() {
        return beratKg;
    }

    public void setBeratKg(int beratKg) {
        this.beratKg = beratKg;
    }

    public boolean isReadyForDelivery() {
        return readyForDelivery;
    }

    public void setReadyForDelivery(boolean readyForDelivery) {
        this.readyForDelivery = readyForDelivery;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

