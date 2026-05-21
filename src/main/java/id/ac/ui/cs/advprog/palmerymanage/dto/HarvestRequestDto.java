package id.ac.ui.cs.advprog.palmerymanage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class HarvestRequestDto {
    @NotNull(message = "ID Kebun (plantationId) tidak boleh kosong")
    private UUID plantationId;
    private UUID mandorId;
    private LocalDate harvestDate;
    private Float kgHarvested;
    private String notes;
    private List<PhotoDto> photos;

    @Data
    public static class PhotoDto {
        private String url;
        private String filename;
        private Integer sizeBytes;
    }
}