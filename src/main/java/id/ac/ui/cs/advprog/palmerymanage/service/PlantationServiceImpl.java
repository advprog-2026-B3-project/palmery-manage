package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.dto.*;
import id.ac.ui.cs.advprog.palmerymanage.exception.*;
import id.ac.ui.cs.advprog.palmerymanage.model.Plantation;
import id.ac.ui.cs.advprog.palmerymanage.model.PlantationAssignment;
import id.ac.ui.cs.advprog.palmerymanage.model.PlantationAssignment.PersonnelRole;
import id.ac.ui.cs.advprog.palmerymanage.repository.PlantationAssignmentRepository;
import id.ac.ui.cs.advprog.palmerymanage.repository.PlantationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlantationServiceImpl implements PlantationService {

    private final PlantationRepository plantationRepository;
    private final PlantationAssignmentRepository assignmentRepository;
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
        PlantationResponseDto response = plantationMapper.toResponseDto(plantation);
        enrichWithAssignments(response, id);
        return response;
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
        log.info("Plantation created: id={}, code={}", saved.getId(), saved.getCode());
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
        log.info("Plantation updated: id={}, code={}", saved.getId(), saved.getCode());

        PlantationResponseDto response = plantationMapper.toResponseDto(saved);
        enrichWithAssignments(response, id);
        return response;
    }

    @Override
    @Transactional
    public void deletePlantation(UUID id) {
        Plantation plantation = findPlantationOrThrow(id);
        ensureNoActivePersonnel(plantation);
        plantationRepository.delete(plantation);
        log.info("Plantation deleted: id={}", id);
    }

    @Override
    @Transactional
    public void assignMandor(UUID plantationId, UUID mandorId) {
        findPlantationOrThrow(plantationId);

        if (assignmentRepository.existsByPlantationIdAndPersonnelIdAndRole(
                plantationId, mandorId, PersonnelRole.MANDOR)) {
            throw new BadRequestException("Mandor sudah ditugaskan ke kebun ini");
        }
        ensurePersonnelNotAssignedElsewhere(mandorId, PersonnelRole.MANDOR, "Mandor");

        PlantationAssignment assignment = PlantationAssignment.builder()
                .plantationId(plantationId)
                .personnelId(mandorId)
                .role(PersonnelRole.MANDOR)
                .build();
        assignmentRepository.save(assignment);
        log.info("Mandor {} assigned to plantation {}", mandorId, plantationId);
    }

    @Override
    @Transactional
    public void unassignMandor(UUID plantationId, UUID mandorId) {
        findPlantationOrThrow(plantationId);

        PlantationAssignment assignment = assignmentRepository
                .findByPlantationIdAndPersonnelIdAndRole(plantationId, mandorId, PersonnelRole.MANDOR)
                .orElseThrow(() -> new BadRequestException("Mandor tidak ditemukan di kebun ini"));

        assignmentRepository.delete(assignment);
        log.info("Mandor {} unassigned from plantation {}", mandorId, plantationId);
    }

    @Override
    @Transactional
    public void transferMandor(UUID fromPlantationId, UUID toPlantationId, UUID mandorId) {
        transferPersonnel(fromPlantationId, toPlantationId, mandorId, PersonnelRole.MANDOR, "Mandor");
    }

    @Override
    @Transactional
    public void assignSupir(UUID plantationId, UUID supirId) {
        findPlantationOrThrow(plantationId);

        if (assignmentRepository.existsByPlantationIdAndPersonnelIdAndRole(
                plantationId, supirId, PersonnelRole.SUPIR)) {
            throw new BadRequestException("Supir sudah ditugaskan ke kebun ini");
        }
        ensurePersonnelNotAssignedElsewhere(supirId, PersonnelRole.SUPIR, "Supir");

        PlantationAssignment assignment = PlantationAssignment.builder()
                .plantationId(plantationId)
                .personnelId(supirId)
                .role(PersonnelRole.SUPIR)
                .build();
        assignmentRepository.save(assignment);
        log.info("Supir {} assigned to plantation {}", supirId, plantationId);
    }

    @Override
    @Transactional
    public void unassignSupir(UUID plantationId, UUID supirId) {
        findPlantationOrThrow(plantationId);

        PlantationAssignment assignment = assignmentRepository
                .findByPlantationIdAndPersonnelIdAndRole(plantationId, supirId, PersonnelRole.SUPIR)
                .orElseThrow(() -> new BadRequestException("Supir tidak ditemukan di kebun ini"));

        assignmentRepository.delete(assignment);
        log.info("Supir {} unassigned from plantation {}", supirId, plantationId);
    }

    @Override
    @Transactional
    public void transferSupir(UUID fromPlantationId, UUID toPlantationId, UUID supirId) {
        transferPersonnel(fromPlantationId, toPlantationId, supirId, PersonnelRole.SUPIR, "Supir");
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
        boolean hasActiveMandor = assignmentRepository.existsByPlantationIdAndRole(
                plantation.getId(), PersonnelRole.MANDOR);
        boolean hasActiveSupir = assignmentRepository.existsByPlantationIdAndRole(
                plantation.getId(), PersonnelRole.SUPIR);
        if (hasActiveMandor || hasActiveSupir) {
            throw new PlantationHasActivePersonnelException(plantation.getId());
        }
    }

    private void ensurePersonnelNotAssignedElsewhere(UUID personnelId, PersonnelRole role, String label) {
        if (!assignmentRepository.findByPersonnelIdAndRole(personnelId, role).isEmpty()) {
            throw new BadRequestException(label + " sudah ditugaskan ke kebun lain; gunakan transfer");
        }
    }

    private void transferPersonnel(UUID fromPlantationId, UUID toPlantationId, UUID personnelId,
                                   PersonnelRole role, String label) {
        findPlantationOrThrow(fromPlantationId);
        findPlantationOrThrow(toPlantationId);

        if (fromPlantationId.equals(toPlantationId)) {
            throw new BadRequestException("Kebun asal dan tujuan tidak boleh sama");
        }

        PlantationAssignment currentAssignment = assignmentRepository
                .findByPlantationIdAndPersonnelIdAndRole(fromPlantationId, personnelId, role)
                .orElseThrow(() -> new BadRequestException(label + " tidak ditemukan di kebun asal"));

        if (assignmentRepository.existsByPlantationIdAndPersonnelIdAndRole(toPlantationId, personnelId, role)) {
            throw new BadRequestException(label + " sudah ditugaskan ke kebun tujuan");
        }

        assignmentRepository.delete(currentAssignment);
        assignmentRepository.save(PlantationAssignment.builder()
                .plantationId(toPlantationId)
                .personnelId(personnelId)
                .role(role)
                .build());
        log.info("{} {} transferred from plantation {} to {}", label, personnelId, fromPlantationId, toPlantationId);
    }

    private void enrichWithAssignments(PlantationResponseDto response, UUID plantationId) {
        List<PlantationAssignment> assignments = assignmentRepository.findByPlantationId(plantationId);

        List<UUID> mandorIds = assignments.stream()
                .filter(a -> a.getRole() == PersonnelRole.MANDOR)
                .map(PlantationAssignment::getPersonnelId)
                .toList();

        List<UUID> supirIds = assignments.stream()
                .filter(a -> a.getRole() == PersonnelRole.SUPIR)
                .map(PlantationAssignment::getPersonnelId)
                .toList();

        response.setAssignedMandorIds(mandorIds);
        response.setAssignedSupirIds(supirIds);
    }
}
