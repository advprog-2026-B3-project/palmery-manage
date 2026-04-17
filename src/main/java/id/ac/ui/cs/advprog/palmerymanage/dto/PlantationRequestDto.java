package id.ac.ui.cs.advprog.palmerymanage.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantationRequestDto {

    @NotBlank(message = "Nama kebun tidak boleh kosong")
    private String name;

    @NotBlank(message = "Kode kebun tidak boleh kosong")
    private String code;

    @NotNull(message = "Luas kebun tidak boleh kosong")
    @Positive(message = "Luas kebun harus bernilai positif")
    private Double areaHa;

    // Top-Left
    @NotNull @DecimalMin("-90.0") @DecimalMax("90.0")
    private Double coordTlLat;

    @NotNull @DecimalMin("-180.0") @DecimalMax("180.0")
    private Double coordTlLon;

    // Top-Right
    @NotNull @DecimalMin("-90.0") @DecimalMax("90.0")
    private Double coordTrLat;

    @NotNull @DecimalMin("-180.0") @DecimalMax("180.0")
    private Double coordTrLon;

    // Bottom-Right
    @NotNull @DecimalMin("-90.0") @DecimalMax("90.0")
    private Double coordBrLat;

    @NotNull @DecimalMin("-180.0") @DecimalMax("180.0")
    private Double coordBrLon;

    // Bottom-Left
    @NotNull @DecimalMin("-90.0") @DecimalMax("90.0")
    private Double coordBlLat;

    @NotNull @DecimalMin("-180.0") @DecimalMax("180.0")
    private Double coordBlLon;
}
