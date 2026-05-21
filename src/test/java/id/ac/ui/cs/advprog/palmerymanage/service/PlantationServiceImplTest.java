package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.dto.*;
import id.ac.ui.cs.advprog.palmerymanage.exception.*;
import id.ac.ui.cs.advprog.palmerymanage.model.Plantation;
import id.ac.ui.cs.advprog.palmerymanage.model.PlantationAssignment;
import id.ac.ui.cs.advprog.palmerymanage.model.PlantationAssignment.PersonnelRole;
import id.ac.ui.cs.advprog.palmerymanage.repository.PlantationAssignmentRepository;
import id.ac.ui.cs.advprog.palmerymanage.repository.PlantationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlantationServiceImplTest {

    @Mock
    private PlantationRepository plantationRepository;

    @Mock
    private PlantationAssignmentRepository assignmentRepository;

    @Spy
    private PlantationMapper plantationMapper = new PlantationMapper();

    @Mock
    private PlantationCoordinateValidator coordinateValidator;

    @InjectMocks
    private PlantationServiceImpl plantationService;

    private PlantationRequestDto validRequest;
    private Plantation samplePlantation;
    private UUID plantationId;

    @BeforeEach
    void setUp() {
        plantationId = UUID.randomUUID();

        validRequest = PlantationRequestDto.builder()
                .name("Kebun Sawit A")
                .code("KS-001")
                .areaHa(50.0)
                .coordTlLat(-6.1)
                .coordTlLon(106.8)
                .coordTrLat(-6.1)
                .coordTrLon(106.9)
                .coordBrLat(-6.2)
                .coordBrLon(106.9)
                .coordBlLat(-6.2)
                .coordBlLon(106.8)
                .build();

        samplePlantation = Plantation.builder()
                .id(plantationId)
                .name("Kebun Sawit A")
                .code("KS-001")
                .areaHa(50.0)
                .coordTlLat(-6.1)
                .coordTlLon(106.8)
                .coordTrLat(-6.1)
                .coordTrLon(106.9)
                .coordBrLat(-6.2)
                .coordBrLon(106.9)
                .coordBlLat(-6.2)
                .coordBlLon(106.8)
                .isActive(true)
                .build();
    }

    @Nested
    class CreatePlantation {

        @Test
        void shouldCreatePlantationSuccessfully() {
            when(plantationRepository.existsByCode("KS-001")).thenReturn(false);
            when(plantationRepository.findAll()).thenReturn(List.of());
            when(coordinateValidator.hasOverlapWithAny(any(), anyList())).thenReturn(false);
            when(plantationRepository.save(any(Plantation.class))).thenReturn(samplePlantation);

            PlantationResponseDto result = plantationService.createPlantation(validRequest);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Kebun Sawit A");
            assertThat(result.getCode()).isEqualTo("KS-001");
            verify(plantationRepository).save(any(Plantation.class));
        }

        @Test
        void shouldThrowWhenCodeAlreadyExists() {
            when(plantationRepository.existsByCode("KS-001")).thenReturn(true);

            assertThatThrownBy(() -> plantationService.createPlantation(validRequest))
                    .isInstanceOf(PlantationCodeAlreadyExistsException.class);
        }

        @Test
        void shouldThrowWhenCoordinatesOverlap() {
            when(plantationRepository.existsByCode("KS-001")).thenReturn(false);
            when(plantationRepository.findAll()).thenReturn(List.of(samplePlantation));
            when(coordinateValidator.hasOverlapWithAny(any(), anyList())).thenReturn(true);

            assertThatThrownBy(() -> plantationService.createPlantation(validRequest))
                    .isInstanceOf(PlantationOverlapException.class);
        }
    }

    @Nested
    class GetPlantation {

        @Test
        void shouldGetPlantationById() {
            when(plantationRepository.findById(plantationId)).thenReturn(Optional.of(samplePlantation));
            when(assignmentRepository.findByPlantationId(plantationId)).thenReturn(List.of());

            PlantationResponseDto result = plantationService.getPlantationById(plantationId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(plantationId);
            assertThat(result.getAssignedMandorIds()).isEmpty();
            assertThat(result.getAssignedSupirIds()).isEmpty();
        }

        @Test
        void shouldThrowWhenPlantationNotFound() {
            when(plantationRepository.findById(plantationId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> plantationService.getPlantationById(plantationId))
                    .isInstanceOf(PlantationNotFoundException.class);
        }

        @Test
        void shouldReturnAllPlantations() {
            when(plantationRepository.findAllByFilter(null, null)).thenReturn(List.of(samplePlantation));

            List<PlantationSummaryDto> result = plantationService.getAllPlantations(null, null);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Kebun Sawit A");
        }

        @Test
        void shouldSearchByName() {
            when(plantationRepository.findAllByFilter("Sawit", null)).thenReturn(List.of(samplePlantation));

            List<PlantationSummaryDto> result = plantationService.getAllPlantations("Sawit", null);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    class UpdatePlantation {

        @Test
        void shouldUpdatePlantationSuccessfully() {
            PlantationRequestDto updateRequest = PlantationRequestDto.builder()
                    .name("Kebun Sawit B")
                    .code("KS-002")
                    .areaHa(75.0)
                    .coordTlLat(-6.1)
                    .coordTlLon(107.0)
                    .coordTrLat(-6.1)
                    .coordTrLon(107.1)
                    .coordBrLat(-6.2)
                    .coordBrLon(107.1)
                    .coordBlLat(-6.2)
                    .coordBlLon(107.0)
                    .build();

            when(plantationRepository.findById(plantationId)).thenReturn(Optional.of(samplePlantation));
            when(plantationRepository.existsByCodeAndIdNot("KS-002", plantationId)).thenReturn(false);
            when(plantationRepository.findAllByIdNot(plantationId)).thenReturn(List.of());
            when(coordinateValidator.hasOverlapWithAny(any(), anyList())).thenReturn(false);
            when(plantationRepository.save(any(Plantation.class))).thenReturn(samplePlantation);
            when(assignmentRepository.findByPlantationId(plantationId)).thenReturn(List.of());

            PlantationResponseDto result = plantationService.updatePlantation(plantationId, updateRequest);

            assertThat(result).isNotNull();
            verify(plantationRepository).save(any(Plantation.class));
        }

        @Test
        void shouldThrowWhenUpdatingNonExistentPlantation() {
            when(plantationRepository.findById(plantationId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> plantationService.updatePlantation(plantationId, validRequest))
                    .isInstanceOf(PlantationNotFoundException.class);
        }

        @Test
        void shouldThrowWhenUpdatingWithDuplicateCode() {
            PlantationRequestDto updateRequest = PlantationRequestDto.builder()
                    .name("Kebun Sawit B")
                    .code("KS-002")
                    .build();

            when(plantationRepository.findById(plantationId)).thenReturn(Optional.of(samplePlantation));
            when(plantationRepository.existsByCodeAndIdNot("KS-002", plantationId)).thenReturn(true);

            assertThatThrownBy(() -> plantationService.updatePlantation(plantationId, updateRequest))
                    .isInstanceOf(PlantationCodeAlreadyExistsException.class);
        }
    }

    @Nested
    class DeletePlantation {

        @Test
        void shouldDeletePlantationWhenNoMandorAssigned() {
            when(plantationRepository.findById(plantationId)).thenReturn(Optional.of(samplePlantation));
            when(assignmentRepository.existsByPlantationIdAndRole(plantationId, PersonnelRole.MANDOR))
                    .thenReturn(false);

            plantationService.deletePlantation(plantationId);

            verify(plantationRepository).delete(samplePlantation);
        }

        @Test
        void shouldThrowWhenDeletingPlantationWithActiveMandor() {
            when(plantationRepository.findById(plantationId)).thenReturn(Optional.of(samplePlantation));
            when(assignmentRepository.existsByPlantationIdAndRole(plantationId, PersonnelRole.MANDOR))
                    .thenReturn(true);

            assertThatThrownBy(() -> plantationService.deletePlantation(plantationId))
                    .isInstanceOf(PlantationHasActivePersonnelException.class);

            verify(plantationRepository, never()).delete(any());
        }

        @Test
        void shouldThrowWhenDeletingPlantationWithActiveSupir() {
            when(plantationRepository.findById(plantationId)).thenReturn(Optional.of(samplePlantation));
            when(assignmentRepository.existsByPlantationIdAndRole(plantationId, PersonnelRole.MANDOR))
                    .thenReturn(false);
            when(assignmentRepository.existsByPlantationIdAndRole(plantationId, PersonnelRole.SUPIR))
                    .thenReturn(true);

            assertThatThrownBy(() -> plantationService.deletePlantation(plantationId))
                    .isInstanceOf(PlantationHasActivePersonnelException.class);

            verify(plantationRepository, never()).delete(any());
        }
    }

    @Nested
    class AssignMandor {

        @Test
        void shouldAssignMandorSuccessfully() {
            UUID mandorId = UUID.randomUUID();
            when(plantationRepository.findById(plantationId)).thenReturn(Optional.of(samplePlantation));
            when(assignmentRepository.existsByPlantationIdAndPersonnelIdAndRole(
                    plantationId, mandorId, PersonnelRole.MANDOR)).thenReturn(false);
            when(assignmentRepository.save(any(PlantationAssignment.class)))
                    .thenReturn(PlantationAssignment.builder().build());

            plantationService.assignMandor(plantationId, mandorId);

            verify(assignmentRepository).save(argThat(a ->
                    a.getPlantationId().equals(plantationId) &&
                    a.getPersonnelId().equals(mandorId) &&
                    a.getRole() == PersonnelRole.MANDOR
            ));
        }

        @Test
        void shouldThrowWhenMandorAlreadyAssigned() {
            UUID mandorId = UUID.randomUUID();
            when(plantationRepository.findById(plantationId)).thenReturn(Optional.of(samplePlantation));
            when(assignmentRepository.existsByPlantationIdAndPersonnelIdAndRole(
                    plantationId, mandorId, PersonnelRole.MANDOR)).thenReturn(true);

            assertThatThrownBy(() -> plantationService.assignMandor(plantationId, mandorId))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("sudah ditugaskan");
        }

        @Test
        void shouldUnassignMandorSuccessfully() {
            UUID mandorId = UUID.randomUUID();
            PlantationAssignment assignment = PlantationAssignment.builder()
                    .id(UUID.randomUUID())
                    .plantationId(plantationId)
                    .personnelId(mandorId)
                    .role(PersonnelRole.MANDOR)
                    .build();

            when(plantationRepository.findById(plantationId)).thenReturn(Optional.of(samplePlantation));
            when(assignmentRepository.findByPlantationIdAndPersonnelIdAndRole(
                    plantationId, mandorId, PersonnelRole.MANDOR)).thenReturn(Optional.of(assignment));

            plantationService.unassignMandor(plantationId, mandorId);

            verify(assignmentRepository).delete(assignment);
        }

        @Test
        void shouldThrowWhenUnassigningNonExistentMandor() {
            UUID mandorId = UUID.randomUUID();
            when(plantationRepository.findById(plantationId)).thenReturn(Optional.of(samplePlantation));
            when(assignmentRepository.findByPlantationIdAndPersonnelIdAndRole(
                    plantationId, mandorId, PersonnelRole.MANDOR)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> plantationService.unassignMandor(plantationId, mandorId))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("tidak ditemukan");
        }
    }

    @Nested
    class AssignSupir {

        @Test
        void shouldAssignSupirSuccessfully() {
            UUID supirId = UUID.randomUUID();
            when(plantationRepository.findById(plantationId)).thenReturn(Optional.of(samplePlantation));
            when(assignmentRepository.existsByPlantationIdAndPersonnelIdAndRole(
                    plantationId, supirId, PersonnelRole.SUPIR)).thenReturn(false);
            when(assignmentRepository.save(any(PlantationAssignment.class)))
                    .thenReturn(PlantationAssignment.builder().build());

            plantationService.assignSupir(plantationId, supirId);

            verify(assignmentRepository).save(argThat(a ->
                    a.getPlantationId().equals(plantationId) &&
                    a.getPersonnelId().equals(supirId) &&
                    a.getRole() == PersonnelRole.SUPIR
            ));
        }

        @Test
        void shouldThrowWhenSupirAlreadyAssigned() {
            UUID supirId = UUID.randomUUID();
            when(plantationRepository.findById(plantationId)).thenReturn(Optional.of(samplePlantation));
            when(assignmentRepository.existsByPlantationIdAndPersonnelIdAndRole(
                    plantationId, supirId, PersonnelRole.SUPIR)).thenReturn(true);

            assertThatThrownBy(() -> plantationService.assignSupir(plantationId, supirId))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("sudah ditugaskan");
        }

        @Test
        void shouldUnassignSupirSuccessfully() {
            UUID supirId = UUID.randomUUID();
            PlantationAssignment assignment = PlantationAssignment.builder()
                    .id(UUID.randomUUID())
                    .plantationId(plantationId)
                    .personnelId(supirId)
                    .role(PersonnelRole.SUPIR)
                    .build();

            when(plantationRepository.findById(plantationId)).thenReturn(Optional.of(samplePlantation));
            when(assignmentRepository.findByPlantationIdAndPersonnelIdAndRole(
                    plantationId, supirId, PersonnelRole.SUPIR)).thenReturn(Optional.of(assignment));

            plantationService.unassignSupir(plantationId, supirId);

            verify(assignmentRepository).delete(assignment);
        }

        @Test
        void shouldThrowWhenUnassigningNonExistentSupir() {
            UUID supirId = UUID.randomUUID();
            when(plantationRepository.findById(plantationId)).thenReturn(Optional.of(samplePlantation));
            when(assignmentRepository.findByPlantationIdAndPersonnelIdAndRole(
                    plantationId, supirId, PersonnelRole.SUPIR)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> plantationService.unassignSupir(plantationId, supirId))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("tidak ditemukan");
        }
    }

    @Nested
    class ResponseEnrichment {

        @Test
        void shouldIncludeAssignedPersonnelInResponse() {
            UUID mandorId = UUID.randomUUID();
            UUID supirId = UUID.randomUUID();

            List<PlantationAssignment> assignments = List.of(
                    PlantationAssignment.builder()
                            .plantationId(plantationId)
                            .personnelId(mandorId)
                            .role(PersonnelRole.MANDOR)
                            .build(),
                    PlantationAssignment.builder()
                            .plantationId(plantationId)
                            .personnelId(supirId)
                            .role(PersonnelRole.SUPIR)
                            .build()
            );

            when(plantationRepository.findById(plantationId)).thenReturn(Optional.of(samplePlantation));
            when(assignmentRepository.findByPlantationId(plantationId)).thenReturn(assignments);

            PlantationResponseDto result = plantationService.getPlantationById(plantationId);

            assertThat(result.getAssignedMandorIds()).containsExactly(mandorId);
            assertThat(result.getAssignedSupirIds()).containsExactly(supirId);
        }
    }
}
