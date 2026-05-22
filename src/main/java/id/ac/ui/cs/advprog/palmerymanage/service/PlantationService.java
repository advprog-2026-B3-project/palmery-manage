package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.dto.*;

import java.util.List;
import java.util.UUID;

public interface PlantationService {

    List<PlantationSummaryDto> getAllPlantations(String name, String code);

    PlantationResponseDto getPlantationById(UUID id);

    PlantationResponseDto createPlantation(PlantationRequestDto request);

    PlantationResponseDto updatePlantation(UUID id, PlantationRequestDto request);

    void deletePlantation(UUID id);

    void assignMandor(UUID plantationId, UUID mandorId);

    void unassignMandor(UUID plantationId, UUID mandorId);

    void transferMandor(UUID fromPlantationId, UUID toPlantationId, UUID mandorId);

    void assignSupir(UUID plantationId, UUID supirId);

    void unassignSupir(UUID plantationId, UUID supirId);

    void transferSupir(UUID fromPlantationId, UUID toPlantationId, UUID supirId);
}
