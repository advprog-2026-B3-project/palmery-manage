package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.dto.HarvestSummaryDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ========================================================
 * PUBLIC CONTRACT — HarvestProviderService
 * ========================================================
 *
 * Interface ini adalah satu-satunya pintu masuk resmi ke modul Harvest.
 * Modul lain (Delivery, Plantation, dll) WAJIB menggunakan interface ini.
 * DILARANG keras mengakses HarvestResultRepository atau HarvestService langsung.
 *
 * Prinsip yang diterapkan:
 * - Dependency Inversion Principle (DIP): Modul lain bergantung pada
 *   abstraksi (interface ini), bukan pada implementasi konkret.
 * - Interface Segregation: Hanya method yang benar-benar dibutuhkan
 *   oleh modul lain yang didefinisikan di sini.
 *
 * Cara penggunaan oleh modul lain:
 *   @Autowired
 *   private HarvestProviderService harvestProvider;
 *
 *   // Lalu panggil method yang tersedia:
 *   boolean valid = harvestProvider.isReadyForDelivery(harvestId);
 */
public interface HarvestProviderService {

    /**
     * Mengecek apakah laporan panen sudah disetujui Mandor dan
     * siap untuk dijemput oleh Supir (Delivery module).
     *
     * @param harvestId UUID laporan panen
     * @return true jika status == APPROVED dan readyForDelivery == true
     * @throws IllegalArgumentException jika harvestId tidak ditemukan
     */
    boolean isReadyForDelivery(UUID harvestId);

    /**
     * Mengambil status validasi terkini dari sebuah laporan panen.
     * Berguna untuk modul lain yang perlu menampilkan status.
     *
     * @param harvestId UUID laporan panen
     * @return String status: "PENDING", "APPROVED", atau "REJECTED"
     * @throws IllegalArgumentException jika harvestId tidak ditemukan
     */
    String getHarvestStatus(UUID harvestId);

    /**
     * Mengambil ringkasan data panen berdasarkan ID-nya.
     * Mengembalikan Optional.empty() jika tidak ditemukan (aman, tidak melempar exception).
     *
     * @param harvestId UUID laporan panen
     * @return Optional berisi HarvestSummaryDto jika ditemukan
     */
    Optional<HarvestSummaryDto> findHarvestSummary(UUID harvestId);

    /**
     * Mengambil semua laporan panen milik seorang Buruh
     * yang sudah disetujui (APPROVED) dan siap dikirim.
     * Berguna untuk modul Delivery saat menjadwalkan pengiriman.
     *
     * @param workerId UUID buruh
     * @return List laporan panen yang siap dikirim
     */
    List<HarvestSummaryDto> getApprovedHarvestsByWorker(UUID workerId);

    /**
     * Mengambil semua laporan panen dari sebuah kebun
     * yang sudah disetujui (APPROVED).
     * Berguna untuk modul Plantation melihat histori produksi.
     *
     * @param plantationId UUID kebun
     * @return List laporan panen APPROVED dari kebun tersebut
     */
    List<HarvestSummaryDto> getApprovedHarvestsByPlantation(UUID plantationId);
}
