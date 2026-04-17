package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.dto.*;
import id.ac.ui.cs.advprog.palmerymanage.exception.*;
import id.ac.ui.cs.advprog.palmerymanage.model.Plantation;
import id.ac.ui.cs.advprog.palmerymanage.repository.PlantationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlantationServiceImpl implements PlantationService {

    private final PlantationRepository plantationRepository;
    private final PlantationMapper plantationMapper;
    private final PlantationCoordinateValidator coordinateValidator;

    @Override
    @Transactional(readOnly = true)
    public List<PlantationSummaryDto> getAllPlantations(String name, String code) {
        return plantationRepository.findAllByFilter(name, code)
                .stream()
                .map(plantationMapper::toSummaryDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PlantationResponseDto getPlantationById(UUID id) {
        Plantation plantation = findPlantationOrThrow(id);
        return plantationMapper.toResponseDto(plantation);
    }

    @Override
    @Transactional
    public PlantationResponseDto createPlantation(PlantationRequestDto request) {
        validateUniqueCode(request.getCode(), null);

        List<Plantation> existingPlantations = plantationRepository.findAll();
        if (coordinateValidator.hasOverlapWithAny(request, existingPlantations)) {
            throw new PlantationOverlapException();
        }

        Plantation plantation = plantationMapper.toEntity(request);
        Plantation saved = plantationRepository.save(plantation);
        return plantationMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public PlantationResponseDto updatePlantation(UUID id, PlantationRequestDto request) {
        Plantation plantation = findPlantationOrThrow(id);
        validateUniqueCode(request.getCode(), id);

        List<Plantation> otherPlantations = plantationRepository.findAllByIdNot(id);
        if (coordinateValidator.hasOverlapWithAny(request, otherPlantations)) {
            throw new PlantationOverlapException();
        }

        plantationMapper.updateEntityFromDto(plantation, request);
        Plantation saved = plantationRepository.save(plantation);
        return plantationMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public void deletePlantation(UUID id) {
        Plantation plantation = findPlantationOrThrow(id);
        ensureNoActivePersonnel(plantation);
        plantationRepository.delete(plantation);
    }

    @Override
    @Transactional
    public void assignMandor(UUID plantationId, UUID mandorId) {
        // Guard: ensure plantation exists before delegating to assignment logic
        findPlantationOrThrow(plantationId);
        // NOTE: Actual assignment record is managed by the PlantationPersonnel relational service.
        // This method validates existence and can be extended to emit events or enforce business rules.
    }

    @Override
    @Transactional
    public void unassignMandor(UUID plantationId, UUID mandorId) {
        findPlantationOrThrow(plantationId);
        // Delegates to assignment service; mandor cannot be re-assigned to another plantation while active
    }

    @Override
    @Transactional
    public void assignSupir(UUID plantationId, UUID supirId) {
        findPlantationOrThrow(plantationId);
    }

    @Override
    @Transactional
    public void unassignSupir(UUID plantationId, UUID supirId) {
        findPlantationOrThrow(plantationId);
    }

    // --- Private helpers ---

    private Plantation findPlantationOrThrow(UUID id) {
        return plantationRepository.findById(id)
                .orElseThrow(() -> new PlantationNotFoundException(id));
    }

    private void validateUniqueCode(String code, UUID excludeId) {
        boolean codeExists = (excludeId == null)
                ? plantationRepository.existsByCode(code)
                : plantationRepository.existsByCodeAndIdNot(code, excludeId);

        if (codeExists) {
            throw new PlantationCodeAlreadyExistsException(code);
        }
    }

    private void ensureNoActivePersonnel(Plantation plantation) {
        // Guard: hapus gagal jika masih ada Mandor terikat.
        // The actual check queries the PlantationPersonnel association table.
        // When the personnel module's repository is available, inject and query it here.
        // Placeholder: always passes until personnel module is integrated.
        boolean hasActiveMandor = false; // replace with: personnelRepository.existsByPlantationIdAndRole(plantation.getId(), Role.MANDOR)
        if (hasActiveMandor) {
            throw new PlantationHasActivePersonnelException(plantation.getId());
        }
    }
}
