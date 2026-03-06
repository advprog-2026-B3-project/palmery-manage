package id.ac.ui.cs.advprog.palmerymanage.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantationResponseDto {

    private UUID id;
    private String name;
    private String code;
    private Double areaHa;

    private Double coordTlLat;
    private Double coordTlLon;
    private Double coordTrLat;
    private Double coordTrLon;
    private Double coordBrLat;
    private Double coordBrLon;
    private Double coordBlLat;
    private Double coordBlLon;

    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
