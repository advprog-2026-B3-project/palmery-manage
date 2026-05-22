package id.ac.ui.cs.advprog.palmerymanage.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class HarvestResponseDto {
    private UUID id;
    private UUID workerId;
    private UUID mandorId;
    private UUID plantationId;
    private LocalDate harvestDate;
    private Float kgHarvested;
    private String notes;
    private Boolean readyForDelivery;
    private String status;
    private String rejectionReason;
    private LocalDateTime validatedAt;
    private LocalDateTime createdAt;
    private List<PhotoDto> photos;

    @Data
    @Builder
    public static class PhotoDto {
        private UUID id;
        private String url;
        private String filename;
        private Integer sizeBytes;
        private LocalDateTime uploadedAt;
    }
}
