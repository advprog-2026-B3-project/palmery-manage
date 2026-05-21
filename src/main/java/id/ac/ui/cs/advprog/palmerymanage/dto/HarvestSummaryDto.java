package id.ac.ui.cs.advprog.palmerymanage.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO yang digunakan sebagai kontrak publik antar-modul.
 * Modul lain (Delivery, Plantation) HANYA boleh menerima data Harvest
 * dalam bentuk DTO ini — BUKAN dalam bentuk entitas HarvestResult.
 *
 * Prinsip: Modul lain tidak perlu tahu detail implementasi internal
 * (status code, foto, catatan, dll). Mereka hanya butuh informasi
 * yang relevan untuk keperluan mereka.
 */
@Data
@Builder
public class HarvestSummaryDto {

    /** ID unik laporan panen. */
    private UUID harvestId;

    /** ID Buruh yang melaporkan panen. */
    private UUID workerId;

    /** ID Mandor yang bertanggung jawab. */
    private UUID mandorId;

    /** ID Kebun tempat panen dilakukan. */
    private UUID plantationId;

    /** Tanggal panen dilaporkan. */
    private LocalDate harvestDate;

    /** Total berat panen dalam kg. */
    private Float kgHarvested;

    /**
     * Status validasi panen saat ini.
     * Nilai yang mungkin: "PENDING", "APPROVED", "REJECTED"
     */
    private String status;

    /**
     * Apakah panen ini siap untuk dijemput/dikirim.
     * Bernilai TRUE hanya jika status == "APPROVED".
     * Ini adalah field utama yang dibutuhkan modul Delivery.
     */
    private boolean readyForDelivery;
}
