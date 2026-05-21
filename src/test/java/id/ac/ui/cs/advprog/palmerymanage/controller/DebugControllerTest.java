package id.ac.ui.cs.advprog.palmerymanage.controller;

import id.ac.ui.cs.advprog.palmerymanage.dto.DebugCheckRequest;
import id.ac.ui.cs.advprog.palmerymanage.model.IntegrationCheck;
import id.ac.ui.cs.advprog.palmerymanage.service.IntegrationDebugService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebugControllerTest {

    @Mock
    private IntegrationDebugService debugService;

    @InjectMocks
    private DebugController debugController;

    @Test
    void healthcheck() {
        when(debugService.integrationStatus()).thenReturn(Map.of("status", "up", "service", "manage"));
        ResponseEntity<Map<String, Object>> response = debugController.healthcheck();
        assertEquals(200, response.getStatusCode().value());
        assertEquals("up", response.getBody().get("status"));
        assertEquals("manage", response.getBody().get("service"));
    }

    @Test
    void integration() {
        when(debugService.integrationStatus()).thenReturn(Map.of("status", "ok"));
        ResponseEntity<Map<String, Object>> response = debugController.integration();
        assertEquals(200, response.getStatusCode().value());
        assertEquals("ok", response.getBody().get("status"));
    }

    @Test
    void createCheck() {
        DebugCheckRequest req = new DebugCheckRequest();
        req.setSource("test-source");
        when(debugService.createCheck("test-source")).thenReturn(Map.of("id", 1L));

        ResponseEntity<Map<String, Object>> response = debugController.createCheck(req);
        assertEquals(201, response.getStatusCode().value());
        assertEquals(1L, response.getBody().get("id"));
    }

    @Test
    void createCheck_nullRequest() {
        when(debugService.createCheck(null)).thenReturn(Map.of("id", 2L));

        ResponseEntity<Map<String, Object>> response = debugController.createCheck(null);
        assertEquals(201, response.getStatusCode().value());
        assertEquals(2L, response.getBody().get("id"));
    }

    @Test
    void latestChecks() {
        IntegrationCheck check = new IntegrationCheck("source");
        ReflectionTestUtils.setField(check, "id", 1L);
        check.prePersist();
        
        when(debugService.latestChecks()).thenReturn(List.of(check));

        ResponseEntity<List<Map<String, Object>>> response = debugController.latestChecks();
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).get("id"));
        assertEquals("source", response.getBody().get(0).get("source"));
        assertNotNull(response.getBody().get(0).get("created_at"));
    }
}
