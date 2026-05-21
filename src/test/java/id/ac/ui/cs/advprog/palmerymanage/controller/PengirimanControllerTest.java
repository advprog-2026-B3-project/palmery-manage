package id.ac.ui.cs.advprog.palmerymanage.controller;

import id.ac.ui.cs.advprog.palmerymanage.dto.CreatePengirimanRequest;
import id.ac.ui.cs.advprog.palmerymanage.dto.PartialRejectRequest;
import id.ac.ui.cs.advprog.palmerymanage.dto.RejectRequest;
import id.ac.ui.cs.advprog.palmerymanage.dto.UpdateStatusRequest;
import id.ac.ui.cs.advprog.palmerymanage.model.HarvestResult;
import id.ac.ui.cs.advprog.palmerymanage.model.Pengiriman;
import id.ac.ui.cs.advprog.palmerymanage.model.PengirimanStatus;
import id.ac.ui.cs.advprog.palmerymanage.model.Plantation;
import id.ac.ui.cs.advprog.palmerymanage.repository.HarvestResultRepository;
import id.ac.ui.cs.advprog.palmerymanage.service.PengirimanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PengirimanControllerTest {

    @Mock
    private PengirimanService pengirimanService;

    @Mock
    private HarvestResultRepository harvestResultRepository;

    @InjectMocks
    private PengirimanController pengirimanController;

    private Authentication authentication;
    private Pengiriman pengiriman;

    @BeforeEach
    void setUp() {
        authentication = mock(Authentication.class);
        pengiriman = new Pengiriman();
        pengiriman.setId(UUID.randomUUID());
        pengiriman.setSupirId("DRV-1");
        pengiriman.setMandorId("MDR-1");
        pengiriman.setKebunId(UUID.randomUUID().toString());
        pengiriman.setTotalKg(100);
        pengiriman.setStatus(PengirimanStatus.MEMUAT);
        pengiriman.setPanenIds(List.of("panen-1"));
        pengiriman.setCreatedAt(Instant.now());
        pengiriman.setUpdatedAt(Instant.now());
    }

    @Test
    void driversForMandor() {
        when(authentication.getName()).thenReturn("MDR-1");
        when(pengirimanService.listSupirOnKebunMandor("MDR-1", "")).thenReturn(List.of());
        ResponseEntity<List<Map<String, Object>>> response = pengirimanController.driversForMandor("MDR-1", authentication, "");
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void driversForMandor_blankId() {
        ResponseEntity<List<Map<String, Object>>> response = pengirimanController.driversForMandor("", null, "");
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void panenSiapAngkut_withAuthName() {
        when(authentication.getName()).thenReturn("MDR-1");

        HarvestResult h1 = new HarvestResult();
        h1.setId(UUID.randomUUID());
        h1.setMandorId(UUID.fromString("00000000-0000-0000-0000-000000000001")); // different mandor
        Plantation p = new Plantation();
        p.setId(UUID.randomUUID());
        h1.setPlantation(p);
        h1.setKgHarvested(100f);
        h1.setStatus("APPROVED");

        HarvestResult h2 = new HarvestResult();
        h2.setId(UUID.randomUUID());
        h2.setStatus("APPROVED");

        when(harvestResultRepository.findByReadyForDeliveryIsTrue()).thenReturn(List.of(h1, h2));

        ResponseEntity<List<Map<String, Object>>> response = pengirimanController.panenSiapAngkut(null, authentication);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isEmpty()); // both filtered out
    }

    @Test
    void panenSiapAngkut_withHeaderFallback_andBlankMandor() {
        HarvestResult h1 = new HarvestResult();
        h1.setId(UUID.randomUUID());
        Plantation p = new Plantation();
        p.setId(UUID.randomUUID());
        h1.setPlantation(p);
        h1.setKgHarvested(100f);
        h1.setStatus("APPROVED");

        when(harvestResultRepository.findByReadyForDeliveryIsTrue()).thenReturn(List.of(h1));

        ResponseEntity<List<Map<String, Object>>> response = pengirimanController.panenSiapAngkut("   ", null);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertNotNull(response.getBody().get(0).get("kebun_id"));
        assertNull(response.getBody().get(0).get("mandor_id"));
    }

    @Test
    void createPengiriman() {
        when(authentication.getName()).thenReturn("MDR-1");
        CreatePengirimanRequest req = new CreatePengirimanRequest("DRV-1", List.of("panen-1"));
        when(pengirimanService.createPengiriman(eq("MDR-1"), any())).thenReturn(pengiriman);

        ResponseEntity<Map<String, Object>> response = pengirimanController.createPengiriman(null, authentication, req);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("MEMUAT", response.getBody().get("status"));
    }

    @Test
    void pengirimanAktifSupir() {
        when(authentication.getName()).thenReturn("DRV-1");
        when(pengirimanService.pengirimanAktifSupir("DRV-1")).thenReturn(List.of(pengiriman));

        ResponseEntity<List<Map<String, Object>>> response = pengirimanController.pengirimanAktifSupir(null, authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void riwayatSupir() {
        when(authentication.getName()).thenReturn("DRV-1");
        when(pengirimanService.riwayatSupir(eq("DRV-1"), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(pengiriman));

        ResponseEntity<List<Map<String, Object>>> response = pengirimanController.riwayatSupir(null, authentication, "2026-01-01", "2026-12-31");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void updateStatusSupir() {
        when(authentication.getName()).thenReturn("DRV-1");
        UpdateStatusRequest req = new UpdateStatusRequest("MENGIRIM");
        when(pengirimanService.updateStatusSupir(eq("DRV-1"), any(), any())).thenReturn(pengiriman);

        ResponseEntity<Map<String, Object>> response = pengirimanController.updateStatusSupir(null, authentication, pengiriman.getId(), req);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void pengirimanAktifMandor() {
        when(authentication.getName()).thenReturn("MDR-1");
        when(pengirimanService.pengirimanAktifMandor("MDR-1")).thenReturn(List.of(pengiriman));

        ResponseEntity<List<Map<String, Object>>> response = pengirimanController.pengirimanAktifMandor(null, authentication);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void pengirimanBySupirForMandor() {
        when(authentication.getName()).thenReturn("MDR-1");
        when(pengirimanService.pengirimanBySupirForMandor(eq("MDR-1"), eq("DRV-1"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(pengiriman));

        ResponseEntity<List<Map<String, Object>>> response = pengirimanController.pengirimanBySupirForMandor(null, authentication, "DRV-1", null, null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void approveMandor() {
        when(authentication.getName()).thenReturn("MDR-1");
        when(pengirimanService.approveByMandor(eq("MDR-1"), any())).thenReturn(pengiriman);

        ResponseEntity<Map<String, Object>> response = pengirimanController.approveMandor(null, authentication, pengiriman.getId());

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void rejectMandor() {
        when(authentication.getName()).thenReturn("MDR-1");
        RejectRequest req = new RejectRequest("reason");
        when(pengirimanService.rejectByMandor(eq("MDR-1"), any(), anyString())).thenReturn(pengiriman);

        ResponseEntity<Map<String, Object>> response = pengirimanController.rejectMandor(null, authentication, pengiriman.getId(), req);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void pendingAdmin() {
        when(pengirimanService.pendingAdmin(null, null)).thenReturn(List.of(pengiriman));

        ResponseEntity<List<Map<String, Object>>> response = pengirimanController.pendingAdmin(null, null);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void detailAdmin() {
        when(pengirimanService.getById(pengiriman.getId())).thenReturn(pengiriman);

        ResponseEntity<Map<String, Object>> response = pengirimanController.detailAdmin(pengiriman.getId());

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void approveAdmin() {
        when(pengirimanService.approveByAdmin(pengiriman.getId())).thenReturn(pengiriman);

        ResponseEntity<Map<String, Object>> response = pengirimanController.approveAdmin(pengiriman.getId());

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void partialRejectAdmin() {
        PartialRejectRequest req = new PartialRejectRequest(50, "reason");
        when(pengirimanService.partialRejectByAdmin(eq(pengiriman.getId()), eq(50), eq("reason"))).thenReturn(pengiriman);

        ResponseEntity<Map<String, Object>> response = pengirimanController.partialRejectAdmin(pengiriman.getId(), req);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void rejectAdmin() {
        RejectRequest req = new RejectRequest("reason");
        when(pengirimanService.rejectByAdmin(eq(pengiriman.getId()), eq("reason"))).thenReturn(pengiriman);

        ResponseEntity<Map<String, Object>> response = pengirimanController.rejectAdmin(pengiriman.getId(), req);

        assertEquals(200, response.getStatusCode().value());
    }
}
