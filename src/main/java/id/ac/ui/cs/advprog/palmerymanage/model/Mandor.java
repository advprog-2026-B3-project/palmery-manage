package id.ac.ui.cs.advprog.palmerymanage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mandor")
public class Mandor {

    @Id
    private String id;

    @Column(nullable = false)
    private String nama;

    @Column(name = "kebun_id", nullable = false)
    private String kebunId;

    private String kontak;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getKebunId() {
        return kebunId;
    }

    public void setKebunId(String kebunId) {
        this.kebunId = kebunId;
    }

    public String getKontak() {
        return kontak;
    }

    public void setKontak(String kontak) {
        this.kontak = kontak;
    }
}

