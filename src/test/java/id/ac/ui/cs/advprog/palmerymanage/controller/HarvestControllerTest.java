package id.ac.ui.cs.advprog.palmerymanage.controller;

import id.ac.ui.cs.advprog.palmerymanage.dto.HarvestRequestDto;
import id.ac.ui.cs.advprog.palmerymanage.dto.ValidationRequestDto;
import id.ac.ui.cs.advprog.palmerymanage.model.HarvestResult;
import id.ac.ui.cs.advprog.palmerymanage.model.Plantation;
import id.ac.ui.cs.advprog.palmerymanage.service.HarvestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class HarvestControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private HarvestService harvestService;

    @InjectMocks
    private HarvestController harvestController;

    private UUID workerId;
    private UUID mandorId;
    private UUID harvestId;
    private HarvestResult sampleHarvest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(harvestController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        workerId = UUID.randomUUID();
        mandorId = UUID.randomUUID();
        harvestId = UUID.randomUUID();

        Plantation plantation = Plantation.builder().id(UUID.randomUUID()).build();
        sampleHarvest = HarvestResult.builder()
                .id(harvestId)
                .workerId(workerId)
                .mandorId(mandorId)
                .plantation(plantation)
                .harvestDate(LocalDate.now())
                .kgHarvested(100f)
                .notes("Panen hari ini lancar")
                .readyForDelivery(false)
                .status("PENDING")
                .build();
    }

    // POST /api/harvests — submitHarvest
    @Test
    void submitHarvest_success_returns201() throws Exception {
        HarvestRequestDto request = new HarvestRequestDto();
        request.setPlantationId(UUID.randomUUID());
        request.setMandorId(mandorId);
        request.setHarvestDate(LocalDate.now());
        request.setKgHarvested(100f);
        request.setNotes("Panen hari ini lancar");

        when(harvestService.submitHarvest(eq(workerId), any())).thenReturn(sampleHarvest);

        mockMvc.perform(post("/api/harvests")
                        .header("X-User-Id", workerId.toString())
                        .header("X-User-Role", "BURUH")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void submitHarvest_wrongRole_returns403() throws Exception {
        HarvestRequestDto request = new HarvestRequestDto();

        mockMvc.perform(post("/api/harvests")
                        .header("X-User-Id", workerId.toString())
                        .header("X-User-Role", "MANDOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void submitHarvest_illegalArgument_returns400() throws Exception {
        HarvestRequestDto request = new HarvestRequestDto();

        when(harvestService.submitHarvest(eq(workerId), any()))
                .thenThrow(new IllegalArgumentException("plantationId kosong"));

        mockMvc.perform(post("/api/harvests")
                        .header("X-User-Id", workerId.toString())
                        .header("X-User-Role", "BURUH")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitHarvest_illegalState_returns409() throws Exception {
        HarvestRequestDto request = new HarvestRequestDto();

        when(harvestService.submitHarvest(eq(workerId), any()))
                .thenThrow(new IllegalStateException("sudah input hari ini"));

        mockMvc.perform(post("/api/harvests")
                        .header("X-User-Id", workerId.toString())
                        .header("X-User-Role", "BURUH")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // PATCH /api/harvests/{id}/validate
    @Test
    void validateHarvest_success_returns200() throws Exception {
        ValidationRequestDto request = new ValidationRequestDto();
        request.setStatus("APPROVED");

        when(harvestService.validateHarvest(eq(mandorId), eq(harvestId), any()))
                .thenReturn(sampleHarvest);

        mockMvc.perform(patch("/api/harvests/{id}/validate", harvestId)
                        .header("X-User-Id", mandorId.toString())
                        .header("X-User-Role", "MANDOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void validateHarvest_wrongRole_returns403() throws Exception {
        ValidationRequestDto request = new ValidationRequestDto();

        mockMvc.perform(patch("/api/harvests/{id}/validate", harvestId)
                        .header("X-User-Id", mandorId.toString())
                        .header("X-User-Role", "BURUH")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void validateHarvest_illegalArgument_returns400() throws Exception {
        ValidationRequestDto request = new ValidationRequestDto();

        when(harvestService.validateHarvest(eq(mandorId), eq(harvestId), any()))
                .thenThrow(new IllegalArgumentException("status kosong"));

        mockMvc.perform(patch("/api/harvests/{id}/validate", harvestId)
                        .header("X-User-Id", mandorId.toString())
                        .header("X-User-Role", "MANDOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validateHarvest_illegalState_returns400() throws Exception {
        ValidationRequestDto request = new ValidationRequestDto();

        when(harvestService.validateHarvest(eq(mandorId), eq(harvestId), any()))
                .thenThrow(new IllegalStateException("sudah divalidasi"));

        mockMvc.perform(patch("/api/harvests/{id}/validate", harvestId)
                        .header("X-User-Id", mandorId.toString())
                        .header("X-User-Role", "MANDOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


    // GET /api/harvests/me
    @Test
    void getMyHarvestHistory_success_returns200() throws Exception {
        when(harvestService.getBuruhHistory(eq(workerId), any(), any(), any()))
                .thenReturn(List.of(sampleHarvest));

        mockMvc.perform(get("/api/harvests/me")
                        .header("X-User-Id", workerId.toString())
                        .header("X-User-Role", "BURUH"))
                .andExpect(status().isOk());
    }

    @Test
    void getMyHarvestHistory_wrongRole_returns403() throws Exception {
        mockMvc.perform(get("/api/harvests/me")
                        .header("X-User-Id", workerId.toString())
                        .header("X-User-Role", "MANDOR"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyHarvestHistory_exception_returns400() throws Exception {
        when(harvestService.getBuruhHistory(eq(workerId), any(), any(), any()))
                .thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/api/harvests/me")
                        .header("X-User-Id", workerId.toString())
                        .header("X-User-Role", "BURUH"))
                .andExpect(status().isBadRequest());
    }

    // GET /api/harvests
    @Test
    void getAllHarvestsForMandor_success_returns200() throws Exception {
        when(harvestService.getMandorHistory(eq(mandorId), any(), any()))
                .thenReturn(List.of(sampleHarvest));

        mockMvc.perform(get("/api/harvests")
                        .header("X-User-Id", mandorId.toString())
                        .header("X-User-Role", "MANDOR"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllHarvestsForMandor_wrongRole_returns403() throws Exception {
        mockMvc.perform(get("/api/harvests")
                        .header("X-User-Id", mandorId.toString())
                        .header("X-User-Role", "BURUH"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllHarvestsForMandor_exception_returns400() throws Exception {
        when(harvestService.getMandorHistory(eq(mandorId), any(), any()))
                .thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/api/harvests")
                        .header("X-User-Id", mandorId.toString())
                        .header("X-User-Role", "MANDOR"))
                .andExpect(status().isBadRequest());
    }

    // GET /api/harvests/{id}
    @Test
    void getHarvestById_found_returns200() throws Exception {
        when(harvestService.getHarvestById(harvestId)).thenReturn(sampleHarvest);

        mockMvc.perform(get("/api/harvests/{id}", harvestId))
                .andExpect(status().isOk());
    }

    @Test
    void getHarvestById_notFound_returns404() throws Exception {
        when(harvestService.getHarvestById(harvestId))
                .thenThrow(new IllegalArgumentException("tidak ditemukan"));

        mockMvc.perform(get("/api/harvests/{id}", harvestId))
                .andExpect(status().isNotFound());
    }

    // GET /api/harvests/worker/{workerId}
    @Test
    void getHarvestsByWorkerId_mandorRole_returns200() throws Exception {
        when(harvestService.getHarvestsByWorkerId(workerId)).thenReturn(List.of(sampleHarvest));

        mockMvc.perform(get("/api/harvests/worker/{workerId}", workerId)
                        .header("X-User-Role", "MANDOR"))
                .andExpect(status().isOk());
    }

    @Test
    void getHarvestsByWorkerId_adminRole_returns200() throws Exception {
        when(harvestService.getHarvestsByWorkerId(workerId)).thenReturn(List.of(sampleHarvest));

        mockMvc.perform(get("/api/harvests/worker/{workerId}", workerId)
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void getHarvestsByWorkerId_wrongRole_returns403() throws Exception {
        mockMvc.perform(get("/api/harvests/worker/{workerId}", workerId)
                        .header("X-User-Role", "BURUH"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getHarvestsByWorkerId_exception_returns400() throws Exception {
        when(harvestService.getHarvestsByWorkerId(workerId))
                .thenThrow(new IllegalArgumentException("error"));

        mockMvc.perform(get("/api/harvests/worker/{workerId}", workerId)
                        .header("X-User-Role", "MANDOR"))
                .andExpect(status().isBadRequest());
    }

    // Additional coverage: mapToResponseDto with photos populated
    @Test
    void submitHarvest_withPhotos_mapsCorrectly() throws Exception {
        // Build a HarvestResult that includes a photo, to cover the photos-stream branch
        id.ac.ui.cs.advprog.palmerymanage.model.HarvestPhoto photo =
                id.ac.ui.cs.advprog.palmerymanage.model.HarvestPhoto.builder()
                        .id(UUID.randomUUID())
                        .url("http://rustfs/panen.jpg")
                        .filename("panen.jpg")
                        .sizeBytes(1024)
                        .uploadedAt(java.time.LocalDateTime.now())
                        .build();

        HarvestResult harvestWithPhoto = HarvestResult.builder()
                .id(harvestId)
                .workerId(workerId)
                .mandorId(mandorId)
                .plantation(Plantation.builder().id(UUID.randomUUID()).build())
                .harvestDate(LocalDate.now())
                .kgHarvested(100f)
                .notes("Panen dengan foto")
                .readyForDelivery(false)
                .status("PENDING")
                .photos(java.util.List.of(photo))
                .build();

        HarvestRequestDto request = new HarvestRequestDto();
        request.setPlantationId(UUID.randomUUID());
        request.setMandorId(mandorId);
        request.setHarvestDate(LocalDate.now());
        request.setKgHarvested(100f);
        request.setNotes("Panen dengan foto");

        when(harvestService.submitHarvest(eq(workerId), any())).thenReturn(harvestWithPhoto);

        mockMvc.perform(post("/api/harvests")
                        .header("X-User-Id", workerId.toString())
                        .header("X-User-Role", "BURUH")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    // Additional coverage: mapToResponseDto with null photos list
    @Test
    void getHarvestById_withNullPhotos_returns200() throws Exception {
        HarvestResult harvestNullPhotos = HarvestResult.builder()
                .id(harvestId)
                .workerId(workerId)
                .mandorId(mandorId)
                .plantation(Plantation.builder().id(UUID.randomUUID()).build())
                .harvestDate(LocalDate.now())
                .kgHarvested(100f)
                .notes("Test")
                .readyForDelivery(true)
                .status("APPROVED")
                .build();
        // Manually set photos to null to cover null-check branch in mapToResponseDto
        harvestNullPhotos.setPhotos(null);

        when(harvestService.getHarvestById(harvestId)).thenReturn(harvestNullPhotos);

        mockMvc.perform(get("/api/harvests/{id}", harvestId))
                .andExpect(status().isOk());
    }
}
