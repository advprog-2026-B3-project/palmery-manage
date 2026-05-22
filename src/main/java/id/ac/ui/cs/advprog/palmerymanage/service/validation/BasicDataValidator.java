package id.ac.ui.cs.advprog.palmerymanage.service.validation;

import id.ac.ui.cs.advprog.palmerymanage.dto.HarvestRequestDto;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Order(1)
public class BasicDataValidator implements HarvestValidator {
    @Override
    public void validate(UUID workerId, HarvestRequestDto request) {
        if (request.getPlantationId() == null) {
            throw new IllegalArgumentException("ID Kebun (plantationId) tidak boleh kosong");
        }
        if (request.getMandorId() == null) {
            throw new IllegalArgumentException("ID Mandor (mandorId) tidak boleh kosong");
        }
        if (request.getHarvestDate() == null) {
            throw new IllegalArgumentException("Tanggal Harvest (harvestDate) tidak boleh kosong");
        }
        if (request.getKgHarvested() == null || request.getKgHarvested() < 1) {
            throw new IllegalArgumentException("Berat Harvest harus diisi dan minimal 1 kg");
        }
        if (request.getNotes() == null || request.getNotes().trim().isEmpty()) {
            throw new IllegalArgumentException("Catatan (notes) tidak boleh kosong");
        }
        if (request.getPhotos() == null || request.getPhotos().isEmpty()) {
            throw new IllegalArgumentException("Foto bukti panen wajib diunggah minimal 1 foto");
        }
    }
}
