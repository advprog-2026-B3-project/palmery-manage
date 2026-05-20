package id.ac.ui.cs.advprog.palmerymanage.controller;

import id.ac.ui.cs.advprog.palmerymanage.dto.CreatePengirimanRequest;
import id.ac.ui.cs.advprog.palmerymanage.dto.PartialRejectRequest;
import id.ac.ui.cs.advprog.palmerymanage.dto.RejectRequest;
import id.ac.ui.cs.advprog.palmerymanage.dto.UpdateStatusRequest;
import id.ac.ui.cs.advprog.palmerymanage.model.Delivery;
import id.ac.ui.cs.advprog.palmerymanage.model.DeliveryStatus;
import id.ac.ui.cs.advprog.palmerymanage.model.HarvestResult;
import id.ac.ui.cs.advprog.palmerymanage.repository.HarvestResultRepository;
import id.ac.ui.cs.advprog.palmerymanage.service.DeliveryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryControllerTest {

    @Mock
    private DeliveryService deliveryService;

    @Mock
    private HarvestResultRepository harvestResultRepository;

    @InjectMocks
    private DeliveryController deliveryController;

    private Authentication authentication;
    private Delivery delivery;

    @BeforeEach
    void setUp() {
        authentication = mock(Authentication.class);
        delivery = new Delivery();
        delivery.setId(UUID.randomUUID());
        delivery.setSupirId("DRV-1");
        delivery.setMandorId("MDR-1");
        delivery.setKebunId("KBN-1");
        delivery.setTotalKg(100);
        delivery.setStatus(DeliveryStatus.MEMUAT);
        delivery.setPanenIds(List.of("panen-1"));
        delivery.setCreatedAt(Instant.now());
        delivery.setUpdatedAt(Instant.now());
    }

    @Test
    void driversForMandor() {
        ResponseEntity<List<Map<String, Object>>> response = deliveryController.driversForMandor("MDR-1", authentication, "");
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isEmpty());
    }
    
    private void assertTrue(boolean b) {
        assertEquals(true, b);
    }

    @Test
    void panenSiapAngkut_withAuthName() {
        when(authentication.getName()).thenReturn("MDR-1");
        
        HarvestResult h1 = new HarvestResult();
        h1.setId(UUID.randomUUID());
        h1.setMandorId(UUID.fromString("00000000-0000-0000-0000-000000000001")); // different mandor
        
        id.ac.ui.cs.advprog.palmerymanage.model.Plantation p = new id.ac.ui.cs.advprog.palmerymanage.model.Plantation();
        p.setId(UUID.randomUUID());
        h1.setPlantation(p);
        
        h1.setKgHarvested(100f);
        h1.setStatus("APPROVED");

        HarvestResult h2 = new HarvestResult();
        h2.setId(UUID.randomUUID());
        // null mandor id, null plantation, null kg
        h2.setStatus("APPROVED");

        when(harvestResultRepository.findByReadyForDeliveryIsTrue()).thenReturn(List.of(h1, h2));
        
        ResponseEntity<List<Map<String, Object>>> response = deliveryController.panenSiapAngkut(null, authentication);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isEmpty()); // both filtered out
    }

    @Test
    void panenSiapAngkut_withHeaderFallback_andBlankMandor() {
        // authentication is null, test resolveUserId fallback
        HarvestResult h1 = new HarvestResult();
        h1.setId(UUID.randomUUID());
        
        id.ac.ui.cs.advprog.palmerymanage.model.Plantation p = new id.ac.ui.cs.advprog.palmerymanage.model.Plantation();
        p.setId(UUID.randomUUID());
        h1.setPlantation(p);
        
        h1.setKgHarvested(100f);
        h1.setStatus("APPROVED");

        when(harvestResultRepository.findByReadyForDeliveryIsTrue()).thenReturn(List.of(h1));
        
        // Blank mandorId means it doesn't filter by mandor
        ResponseEntity<List<Map<String, Object>>> response = deliveryController.panenSiapAngkut("   ", null);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size()); // h1 included
        assertNotNull(response.getBody().get(0).get("kebun_id"));
        assertNull(response.getBody().get(0).get("mandor_id"));
    }

    @Test
    void createPengiriman_testResolveUserId_defaultFallback() {
        // authentication null, header null
        CreatePengirimanRequest req = new CreatePengirimanRequest("DRV-1", List.of("panen-1"));
        when(deliveryService.createPengiriman(eq("MDR-1"), any())).thenReturn(delivery);

        ResponseEntity<Map<String, Object>> response = deliveryController.createPengiriman(null, null, req);

        assertEquals(200, response.getStatusCode().value());
    }


    @Test
    void createPengiriman() {
        when(authentication.getName()).thenReturn("MDR-1");
        CreatePengirimanRequest req = new CreatePengirimanRequest("DRV-1", List.of("panen-1"));
        when(deliveryService.createPengiriman(eq("MDR-1"), any())).thenReturn(delivery);

        ResponseEntity<Map<String, Object>> response = deliveryController.createPengiriman(null, authentication, req);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("MEMUAT", response.getBody().get("status"));
    }

    @Test
    void pengirimanAktifSupir() {
        when(authentication.getName()).thenReturn("DRV-1");
        when(deliveryService.pengirimanAktifSupir("DRV-1")).thenReturn(List.of(delivery));

        ResponseEntity<List<Map<String, Object>>> response = deliveryController.pengirimanAktifSupir(null, authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void riwayatSupir() {
        when(authentication.getName()).thenReturn("DRV-1");
        when(deliveryService.riwayatSupir(anyString(), any(), any())).thenReturn(List.of(delivery));

        ResponseEntity<List<Map<String, Object>>> response = deliveryController.riwayatSupir(null, authentication, "2026-01-01", "2026-12-31");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void updateStatusSupir() {
        when(authentication.getName()).thenReturn("DRV-1");
        UpdateStatusRequest req = new UpdateStatusRequest("MENGIRIM");
        when(deliveryService.updateStatusSupir(eq("DRV-1"), any(), any())).thenReturn(delivery);

        ResponseEntity<Map<String, Object>> response = deliveryController.updateStatusSupir(null, authentication, delivery.getId(), req);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void pengirimanAktifMandor() {
        when(authentication.getName()).thenReturn("MDR-1");
        when(deliveryService.pengirimanAktifKebun("MDR-1")).thenReturn(List.of(delivery));

        ResponseEntity<List<Map<String, Object>>> response = deliveryController.pengirimanAktifMandor(null, authentication);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void approveMandor() {
        when(authentication.getName()).thenReturn("MDR-1");
        when(deliveryService.approveByMandor(eq("MDR-1"), any())).thenReturn(delivery);

        ResponseEntity<Map<String, Object>> response = deliveryController.approveMandor(null, authentication, delivery.getId());

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void rejectMandor() {
        when(authentication.getName()).thenReturn("MDR-1");
        RejectRequest req = new RejectRequest("reason");
        when(deliveryService.rejectByMandor(eq("MDR-1"), any(), anyString())).thenReturn(delivery);

        ResponseEntity<Map<String, Object>> response = deliveryController.rejectMandor(null, authentication, delivery.getId(), req);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void pendingAdmin() {
        when(deliveryService.pendingAdmin()).thenReturn(List.of(delivery));

        ResponseEntity<List<Map<String, Object>>> response = deliveryController.pendingAdmin();

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void detailAdmin() {
        when(deliveryService.getById(delivery.getId())).thenReturn(delivery);

        ResponseEntity<Map<String, Object>> response = deliveryController.detailAdmin(delivery.getId());

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void approveAdmin() {
        when(deliveryService.approveByAdmin(delivery.getId())).thenReturn(delivery);

        ResponseEntity<Map<String, Object>> response = deliveryController.approveAdmin(delivery.getId());

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void partialRejectAdmin() {
        PartialRejectRequest req = new PartialRejectRequest(50, "reason");
        when(deliveryService.partialRejectByAdmin(eq(delivery.getId()), eq(50), eq("reason"))).thenReturn(delivery);

        ResponseEntity<Map<String, Object>> response = deliveryController.partialRejectAdmin(delivery.getId(), req);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void rejectAdmin() {
        RejectRequest req = new RejectRequest("reason");
        when(deliveryService.rejectByAdmin(eq(delivery.getId()), eq("reason"))).thenReturn(delivery);

        ResponseEntity<Map<String, Object>> response = deliveryController.rejectAdmin(delivery.getId(), req);

        assertEquals(200, response.getStatusCode().value());
    }
}
