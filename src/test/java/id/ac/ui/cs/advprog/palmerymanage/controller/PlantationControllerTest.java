package id.ac.ui.cs.advprog.palmerymanage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.palmerymanage.dto.*;
import id.ac.ui.cs.advprog.palmerymanage.exception.*;
import id.ac.ui.cs.advprog.palmerymanage.service.PlantationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlantationController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for unit test
class PlantationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlantationService plantationService;

    private PlantationRequestDto validRequest;
    private PlantationResponseDto sampleResponse;
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

        sampleResponse = PlantationResponseDto.builder()
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
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .assignedMandorIds(List.of())
                .assignedSupirIds(List.of())
                .build();
    }

    @Nested
    class GetEndpoints {

        @Test
        void shouldGetAllPlantations() throws Exception {
            PlantationSummaryDto summary = PlantationSummaryDto.builder()
                    .id(plantationId)
                    .name("Kebun Sawit A")
                    .code("KS-001")
                    .areaHa(50.0)
                    .isActive(true)
                    .build();

            when(plantationService.getAllPlantations(null, null)).thenReturn(List.of(summary));

            mockMvc.perform(get("/kebun"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Kebun Sawit A"))
                    .andExpect(jsonPath("$[0].code").value("KS-001"));
        }

        @Test
        void shouldGetPlantationById() throws Exception {
            when(plantationService.getPlantationById(plantationId)).thenReturn(sampleResponse);

            mockMvc.perform(get("/kebun/{id}", plantationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Kebun Sawit A"))
                    .andExpect(jsonPath("$.code").value("KS-001"))
                    .andExpect(jsonPath("$.areaHa").value(50.0));
        }

        @Test
        void shouldReturn404WhenPlantationNotFound() throws Exception {
            when(plantationService.getPlantationById(plantationId))
                    .thenThrow(new PlantationNotFoundException(plantationId));

            mockMvc.perform(get("/kebun/{id}", plantationId))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldSearchByName() throws Exception {
            PlantationSummaryDto summary = PlantationSummaryDto.builder()
                    .id(plantationId)
                    .name("Kebun Sawit A")
                    .code("KS-001")
                    .areaHa(50.0)
                    .isActive(true)
                    .build();

            when(plantationService.getAllPlantations("Sawit", null)).thenReturn(List.of(summary));

            mockMvc.perform(get("/kebun").param("name", "Sawit"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Kebun Sawit A"));
        }
    }

    @Nested
    class CreateEndpoint {

        @Test
        void shouldCreatePlantation() throws Exception {
            when(plantationService.createPlantation(any(PlantationRequestDto.class))).thenReturn(sampleResponse);

            mockMvc.perform(post("/kebun")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Kebun Sawit A"));
        }

        @Test
        void shouldReturn400WhenNameIsBlank() throws Exception {
            validRequest.setName("");

            mockMvc.perform(post("/kebun")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400WhenAreaIsNegative() throws Exception {
            validRequest.setAreaHa(-10.0);

            mockMvc.perform(post("/kebun")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn409WhenCodeAlreadyExists() throws Exception {
            when(plantationService.createPlantation(any(PlantationRequestDto.class)))
                    .thenThrow(new PlantationCodeAlreadyExistsException("KS-001"));

            mockMvc.perform(post("/kebun")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isConflict());
        }

        @Test
        void shouldReturn422WhenCoordinatesOverlap() throws Exception {
            when(plantationService.createPlantation(any(PlantationRequestDto.class)))
                    .thenThrow(new PlantationOverlapException());

            mockMvc.perform(post("/kebun")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    @Nested
    class UpdateEndpoint {

        @Test
        void shouldUpdatePlantation() throws Exception {
            when(plantationService.updatePlantation(eq(plantationId), any(PlantationRequestDto.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(put("/kebun/{id}", plantationId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Kebun Sawit A"));
        }
    }

    @Nested
    class DeleteEndpoint {

        @Test
        void shouldDeletePlantation() throws Exception {
            doNothing().when(plantationService).deletePlantation(plantationId);

            mockMvc.perform(delete("/kebun/{id}", plantationId))
                    .andExpect(status().isNoContent());
        }

        @Test
        void shouldReturn409WhenPlantationHasActiveMandor() throws Exception {
            doThrow(new PlantationHasActivePersonnelException(plantationId))
                    .when(plantationService).deletePlantation(plantationId);

            mockMvc.perform(delete("/kebun/{id}", plantationId))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    class AssignmentEndpoints {

        @Test
        void shouldAssignMandor() throws Exception {
            UUID mandorId = UUID.randomUUID();
            AssignPersonnelRequestDto request = new AssignPersonnelRequestDto(mandorId);

            doNothing().when(plantationService).assignMandor(plantationId, mandorId);

            mockMvc.perform(post("/kebun/{id}/mandor", plantationId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        void shouldUnassignMandor() throws Exception {
            UUID mandorId = UUID.randomUUID();
            AssignPersonnelRequestDto request = new AssignPersonnelRequestDto(mandorId);

            doNothing().when(plantationService).unassignMandor(plantationId, mandorId);

            mockMvc.perform(delete("/kebun/{id}/mandor", plantationId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void shouldAssignSupir() throws Exception {
            UUID supirId = UUID.randomUUID();
            AssignPersonnelRequestDto request = new AssignPersonnelRequestDto(supirId);

            doNothing().when(plantationService).assignSupir(plantationId, supirId);

            mockMvc.perform(post("/kebun/{id}/supir", plantationId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        void shouldUnassignSupir() throws Exception {
            UUID supirId = UUID.randomUUID();
            AssignPersonnelRequestDto request = new AssignPersonnelRequestDto(supirId);

            doNothing().when(plantationService).unassignSupir(plantationId, supirId);

            mockMvc.perform(delete("/kebun/{id}/supir", plantationId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }
    }
}
