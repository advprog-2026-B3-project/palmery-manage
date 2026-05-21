package id.ac.ui.cs.advprog.palmerymanage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.palmerymanage.dto.CreatePengirimanRequest;
import id.ac.ui.cs.advprog.palmerymanage.model.HarvestResult;
import id.ac.ui.cs.advprog.palmerymanage.model.Pengiriman;
import id.ac.ui.cs.advprog.palmerymanage.model.PengirimanStatus;
import id.ac.ui.cs.advprog.palmerymanage.pengiriman.PengirimanResponseMapper;
import id.ac.ui.cs.advprog.palmerymanage.repository.HarvestResultRepository;
import id.ac.ui.cs.advprog.palmerymanage.service.PengirimanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PengirimanControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private PengirimanService pengirimanService;
    @Mock
    private HarvestResultRepository harvestResultRepository;

    private final PengirimanResponseMapper pengirimanResponseMapper = new PengirimanResponseMapper();

    private PengirimanController pengirimanController;

    private UUID pengirimanId;
    private UUID mandorId;
    private UUID supirId;
    private UUID panenId;

    @BeforeEach
    void setUp() {
        pengirimanController = new PengirimanController(
                pengirimanService, harvestResultRepository, pengirimanResponseMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(pengirimanController).build();
        objectMapper = new ObjectMapper();

        pengirimanId = UUID.randomUUID();
        mandorId = UUID.randomUUID();
        supirId = UUID.randomUUID();
        panenId = UUID.randomUUID();
    }

    private Pengiriman samplePengiriman() {
        Pengiriman p = new Pengiriman();
        p.setId(pengirimanId);
        p.setSupirId(supirId.toString());
        p.setMandorId(mandorId.toString());
        p.setKebunId(UUID.randomUUID().toString());
        p.setTotalKg(90);
        p.setStatus(PengirimanStatus.MEMUAT);
        p.setPanenIds(List.of(panenId.toString()));
        p.setCreatedAt(Instant.parse("2026-05-20T08:00:00Z"));
        p.setUpdatedAt(Instant.parse("2026-05-20T08:00:00Z"));
        return p;
    }

    @Test
    void driversForMandorReturnsEmptyWhenNoUserId() throws Exception {
        mockMvc.perform(get("/api/mandor/drivers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void driversForMandorReturnsDrivers() throws Exception {
        when(pengirimanService.listSupirOnKebunMandor(mandorId.toString(), ""))
                .thenReturn(List.of(Map.of("id", supirId.toString(), "nama", "Budi", "kebun_id", "k1", "kontak", "")));

        mockMvc.perform(get("/api/mandor/drivers")
                        .header("X-User-Id", mandorId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nama").value("Budi"));
    }

    @Test
    void panenSiapAngkutFiltersByMandorHeader() throws Exception {
        HarvestResult harvest = HarvestResult.builder()
                .id(panenId)
                .mandorId(mandorId)
                .plantationId(UUID.randomUUID())
                .kgHarvested(50f)
                .readyForDelivery(true)
                .status("APPROVED")
                .build();
        when(harvestResultRepository.findByReadyForDeliveryIsTrue()).thenReturn(List.of(harvest));

        mockMvc.perform(get("/api/mandor/panen/siap-angkut")
                        .header("X-User-Id", mandorId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(panenId.toString()));
    }

    @Test
    void createPengirimanReturnsMappedBody() throws Exception {
        Pengiriman created = samplePengiriman();
        when(pengirimanService.createPengiriman(eq(mandorId.toString()), any(CreatePengirimanRequest.class)))
                .thenReturn(created);

        CreatePengirimanRequest body = new CreatePengirimanRequest(supirId.toString(), List.of(panenId.toString()));

        mockMvc.perform(post("/api/mandor/pengiriman")
                        .header("X-User-Id", mandorId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MEMUAT"))
                .andExpect(jsonPath("$.total_kg").value(90));
    }

    @Test
    void pengirimanAktifMandorReturnsList() throws Exception {
        when(pengirimanService.pengirimanAktifMandor(mandorId.toString())).thenReturn(List.of(samplePengiriman()));

        mockMvc.perform(get("/api/mandor/pengiriman/aktif")
                        .header("X-User-Id", mandorId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(pengirimanId.toString()));
    }

    @Test
    void approveMandorEndpoint() throws Exception {
        Pengiriman approved = samplePengiriman();
        approved.setStatus(PengirimanStatus.PENDING_ADMIN_REVIEW);
        when(pengirimanService.approveByMandor(mandorId.toString(), pengirimanId)).thenReturn(approved);

        mockMvc.perform(post("/api/mandor/pengiriman/{id}/approve", pengirimanId)
                        .header("X-User-Id", mandorId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_ADMIN_REVIEW"));
    }

    @Test
    void supirUpdateStatusEndpoint() throws Exception {
        Pengiriman updated = samplePengiriman();
        updated.setStatus(PengirimanStatus.MENGIRIM);
        when(pengirimanService.updateStatusSupir(eq(supirId.toString()), eq(pengirimanId), eq(PengirimanStatus.MENGIRIM)))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/supir/pengiriman/{id}/status", pengirimanId)
                        .header("X-User-Id", supirId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"MENGIRIM\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MENGIRIM"));
    }

    @Test
    void adminPendingEndpoint() throws Exception {
        when(pengirimanService.pendingAdmin(null, null)).thenReturn(List.of(samplePengiriman()));

        mockMvc.perform(get("/api/admin/pengiriman/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mandor_id").value(mandorId.toString()));
    }

    @Test
    void rejectMandorEndpoint() throws Exception {
        Pengiriman rejected = samplePengiriman();
        rejected.setStatus(PengirimanStatus.REJECTED_MANDOR);
        when(pengirimanService.rejectByMandor(mandorId.toString(), pengirimanId, "rusak")).thenReturn(rejected);

        mockMvc.perform(post("/api/mandor/pengiriman/{id}/reject", pengirimanId)
                        .header("X-User-Id", mandorId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"rusak\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED_MANDOR"));
    }

    @Test
    void supirRiwayatEndpoint() throws Exception {
        when(pengirimanService.riwayatSupir(eq(supirId.toString()), any(), any()))
                .thenReturn(List.of(samplePengiriman()));

        mockMvc.perform(get("/api/supir/pengiriman/riwayat")
                        .header("X-User-Id", supirId.toString())
                        .param("from", "2026-01-01")
                        .param("to", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].supir_id").value(supirId.toString()));
    }

    @Test
    void supirAktifEndpoint() throws Exception {
        when(pengirimanService.pengirimanAktifSupir(supirId.toString())).thenReturn(List.of(samplePengiriman()));

        mockMvc.perform(get("/api/supir/pengiriman/aktif")
                        .header("X-User-Id", supirId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("MEMUAT"));
    }

    @Test
    void mandorPengirimanBySupirEndpoint() throws Exception {
        when(pengirimanService.pengirimanBySupirForMandor(
                eq(mandorId.toString()), eq(supirId.toString()), any(), any()))
                .thenReturn(List.of(samplePengiriman()));

        mockMvc.perform(get("/api/mandor/supir/{supirId}/pengiriman", supirId)
                        .header("X-User-Id", mandorId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(pengirimanId.toString()));
    }

    @Test
    void adminDetailEndpoint() throws Exception {
        when(pengirimanService.getById(pengirimanId)).thenReturn(samplePengiriman());

        mockMvc.perform(get("/api/admin/pengiriman/{id}", pengirimanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pengirimanId.toString()));
    }

    @Test
    void adminPartialRejectEndpoint() throws Exception {
        Pengiriman partial = samplePengiriman();
        partial.setStatus(PengirimanStatus.PARTIAL_REJECTED_ADMIN);
        partial.setRecognizedKg(70);
        when(pengirimanService.partialRejectByAdmin(pengirimanId, 70, "kurang")).thenReturn(partial);

        mockMvc.perform(post("/api/admin/pengiriman/{id}/partial-reject", pengirimanId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recognizedKg\":70,\"reason\":\"kurang\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recognized_kg").value(70));
    }

    @Test
    void adminRejectEndpoint() throws Exception {
        Pengiriman rejected = samplePengiriman();
        rejected.setStatus(PengirimanStatus.REJECTED_ADMIN);
        when(pengirimanService.rejectByAdmin(pengirimanId, "tolak")).thenReturn(rejected);

        mockMvc.perform(post("/api/admin/pengiriman/{id}/reject", pengirimanId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"tolak\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED_ADMIN"));
    }

    @Test
    void adminApproveEndpoint() throws Exception {
        Pengiriman approved = samplePengiriman();
        approved.setStatus(PengirimanStatus.APPROVED_ADMIN);
        when(pengirimanService.approveByAdmin(pengirimanId)).thenReturn(approved);

        mockMvc.perform(post("/api/admin/pengiriman/{id}/approve", pengirimanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED_ADMIN"));
    }
}
