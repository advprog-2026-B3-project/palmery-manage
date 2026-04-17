package id.ac.ui.cs.advprog.palmerymanage.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantationSummaryDto {

    private UUID id;
    private String name;
    private String code;
    private Double areaHa;
    private Boolean isActive;
}
