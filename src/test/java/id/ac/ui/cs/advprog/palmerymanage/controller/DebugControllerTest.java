package id.ac.ui.cs.advprog.palmerymanage.controller;

import id.ac.ui.cs.advprog.palmerymanage.dto.DebugCheckRequest;
import id.ac.ui.cs.advprog.palmerymanage.model.IntegrationCheck;
import id.ac.ui.cs.advprog.palmerymanage.service.IntegrationDebugService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DebugControllerTest {

    @Mock
    private IntegrationDebugService integrationDebugService;

    @InjectMocks
    private DebugController debugController;

    @Test
    void healthcheckReturnsServicePayload() {
        Map<String, Object> payload = Map.of("service", "manage", "backend", "up");
        when(integrationDebugService.integrationStatus()).thenReturn(payload);

        ResponseEntity<Map<String, Object>> response = debugController.healthcheck();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(payload, response.getBody());
        verify(integrationDebugService).integrationStatus();
    }

    @Test
    void integrationReturnsServicePayload() {
        Map<String, Object> payload = Map.of("service", "manage", "backend", "up");
        when(integrationDebugService.integrationStatus()).thenReturn(payload);

        ResponseEntity<Map<String, Object>> response = debugController.integration();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(payload, response.getBody());
        verify(integrationDebugService).integrationStatus();
    }

    @Test
    void createCheckReturnsCreatedResponse() {
        DebugCheckRequest request = new DebugCheckRequest();
        request.setSource("manual");

        Map<String, Object> created = Map.of(
                "id", 1L,
                "source", "manual",
                "created_at", Instant.now().toString()
        );
        when(integrationDebugService.createCheck("manual")).thenReturn(created);

        ResponseEntity<Map<String, Object>> response = debugController.createCheck(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(created, response.getBody());
        verify(integrationDebugService).createCheck("manual");
    }

    @Test
    void createCheckHandlesNullRequest() {
        Map<String, Object> created = Map.of(
                "id", 2L,
                "source", "frontend-debug",
                "created_at", Instant.now().toString()
        );
        when(integrationDebugService.createCheck(null)).thenReturn(created);

        ResponseEntity<Map<String, Object>> response = debugController.createCheck(null);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(created, response.getBody());
        verify(integrationDebugService).createCheck(null);
    }

    @Test
    void latestChecksMapsEntityFieldsToResponse() {
        IntegrationCheck check = new IntegrationCheck("frontend-debug");
        check.prePersist();
        ReflectionTestUtils.setField(check, "id", 99L);

        when(integrationDebugService.latestChecks()).thenReturn(List.of(check));

        ResponseEntity<List<Map<String, Object>>> response = debugController.latestChecks();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(99L, response.getBody().getFirst().get("id"));
        assertEquals("frontend-debug", response.getBody().getFirst().get("source"));
        assertNotNull(response.getBody().getFirst().get("created_at"));
    }
}
