package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.config.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service khusus untuk memvalidasi keberadaan kebun (Plantation)
 * dengan memanfaatkan Spring Cache + Caffeine untuk menghindari
 * pemanggilan HTTP berulang ke PlantationService.
 *
 * TTL Cache: 30 menit (dikonfigurasi di CacheConfig.java)
 */
@Service
public class PlantationValidationService {

    private final PlantationService plantationService;

    public PlantationValidationService(PlantationService plantationService) {
        this.plantationService = plantationService;
    }

    /**
     * Memvalidasi bahwa kebun dengan ID tersebut benar-benar ada.
     * Hasil validasi di-cache selama 30 menit (TTL).
     *
     * Jika ID kebun sudah ada di cache → TIDAK ada HTTP call ke PlantationService.
     * Jika ID kebun belum ada di cache → panggil PlantationService, lalu simpan hasilnya.
     *
     * @param plantationId UUID kebun yang ingin divalidasi
     * @throws IllegalArgumentException jika kebun tidak ditemukan
     */
    @Cacheable(value = CacheConfig.PLANTATION_CACHE, key = "#plantationId")
    public boolean validateAndCachePlantation(UUID plantationId) {
        try {
            plantationService.getPlantationById(plantationId);
            return true;
        } catch (Exception e) {
            throw new IllegalArgumentException("Kebun tidak ditemukan");
        }
    }

    /**
     * Menghapus (invalidasi) cache untuk kebun tertentu.
     * Harus dipanggil jika ada perubahan status kebun (misal: dihapus atau dinonaktifkan).
     *
     * @param plantationId UUID kebun yang cache-nya ingin dihapus
     */
    @CacheEvict(value = CacheConfig.PLANTATION_CACHE, key = "#plantationId")
    public void evictPlantationCache(UUID plantationId) {
        // Spring akan otomatis menghapus entry dari cache.
        // Tidak perlu kode tambahan di sini.
    }

    /**
     * Menghapus SELURUH cache kebun sekaligus.
     * Gunakan ini jika terjadi perubahan data besar-besaran (misal: sinkronisasi dari admin).
     */
    @CacheEvict(value = CacheConfig.PLANTATION_CACHE, allEntries = true)
    public void evictAllPlantationCache() {
        // Spring akan otomatis menghapus semua entry dari cache.
    }
}
