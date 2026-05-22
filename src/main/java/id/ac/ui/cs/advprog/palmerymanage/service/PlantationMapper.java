package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.dto.*;
import id.ac.ui.cs.advprog.palmerymanage.model.Plantation;
import org.springframework.stereotype.Component;

// Responsible solely for mapping between Plantation entity and DTOs (SRP).
@Component
public class PlantationMapper {

    public PlantationResponseDto toResponseDto(Plantation plantation) {
        return PlantationResponseDto.builder()
                .id(plantation.getId())
                .name(plantation.getName())
                .code(plantation.getCode())
                .areaHa(plantation.getAreaHa())
                .coordTlLat(plantation.getCoordTlLat())
                .coordTlLon(plantation.getCoordTlLon())
                .coordTrLat(plantation.getCoordTrLat())
                .coordTrLon(plantation.getCoordTrLon())
                .coordBrLat(plantation.getCoordBrLat())
                .coordBrLon(plantation.getCoordBrLon())
                .coordBlLat(plantation.getCoordBlLat())
                .coordBlLon(plantation.getCoordBlLon())
                .isActive(plantation.getIsActive())
                .createdAt(plantation.getCreatedAt())
                .updatedAt(plantation.getUpdatedAt())
                .build();
    }

    public PlantationSummaryDto toSummaryDto(Plantation plantation) {
        return PlantationSummaryDto.builder()
                .id(plantation.getId())
                .name(plantation.getName())
                .code(plantation.getCode())
                .areaHa(plantation.getAreaHa())
                .isActive(plantation.getIsActive())
                .build();
    }

    public Plantation toEntity(PlantationRequestDto dto) {
        return Plantation.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .areaHa(dto.getAreaHa())
                .coordTlLat(dto.getCoordTlLat())
                .coordTlLon(dto.getCoordTlLon())
                .coordTrLat(dto.getCoordTrLat())
                .coordTrLon(dto.getCoordTrLon())
                .coordBrLat(dto.getCoordBrLat())
                .coordBrLon(dto.getCoordBrLon())
                .coordBlLat(dto.getCoordBlLat())
                .coordBlLon(dto.getCoordBlLon())
                .build();
    }

    public void updateEntityFromDto(Plantation plantation, PlantationRequestDto dto) {
        plantation.setName(dto.getName());
        plantation.setCode(dto.getCode());
        plantation.setAreaHa(dto.getAreaHa());
        plantation.setCoordTlLat(dto.getCoordTlLat());
        plantation.setCoordTlLon(dto.getCoordTlLon());
        plantation.setCoordTrLat(dto.getCoordTrLat());
        plantation.setCoordTrLon(dto.getCoordTrLon());
        plantation.setCoordBrLat(dto.getCoordBrLat());
        plantation.setCoordBrLon(dto.getCoordBrLon());
        plantation.setCoordBlLat(dto.getCoordBlLat());
        plantation.setCoordBlLon(dto.getCoordBlLon());
    }
}
