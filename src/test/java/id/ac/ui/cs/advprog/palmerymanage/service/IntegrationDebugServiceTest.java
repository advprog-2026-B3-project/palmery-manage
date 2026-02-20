package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.model.IntegrationCheck;
import id.ac.ui.cs.advprog.palmerymanage.repository.IntegrationCheckRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntegrationDebugServiceTest {

    @Mock
    private IntegrationCheckRepository integrationCheckRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private IntegrationDebugService integrationDebugService;

    @Test
    void integrationStatusReturnsExpectedPayload() {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        when(integrationCheckRepository.count()).thenReturn(7L);
        when(integrationCheckRepository.countBySource("frontend-debug")).thenReturn(5L);

        Map<String, Object> payload = integrationDebugService.integrationStatus();

        assertEquals("manage", payload.get("service"));
        assertEquals("up", payload.get("backend"));
        assertEquals(7L, payload.get("record_count"));
        assertEquals(5L, payload.get("frontend_debug_count"));
        assertNotNull(payload.get("timestamp"));
        Instant.parse(payload.get("timestamp").toString());

        @SuppressWarnings("unchecked")
        Map<String, Object> database = (Map<String, Object>) payload.get("database");
        assertEquals("up", database.get("status"));
        assertEquals(1, database.get("ping"));
        assertTrue(database.containsKey("latency_ms"));
    }

    @Test
    void createCheckUsesDefaultSourceWhenBlank() {
        when(integrationCheckRepository.save(any())).thenAnswer(invocation -> {
            IntegrationCheck saved = invocation.getArgument(0);
            saved.prePersist();
            ReflectionTestUtils.setField(saved, "id", 12L);
            return saved;
        });
        when(integrationCheckRepository.countBySource("frontend-debug")).thenReturn(2L);
        when(integrationCheckRepository.count()).thenReturn(8L);

        Map<String, Object> response = integrationDebugService.createCheck(" ");

        assertEquals(12L, response.get("id"));
        assertEquals("frontend-debug", response.get("source"));
        assertNotNull(response.get("created_at"));
        assertEquals(2L, response.get("source_count"));
        assertEquals(8L, response.get("total_count"));

        verify(integrationCheckRepository).save(argThat(check -> "frontend-debug".equals(check.getSource())));
        verify(integrationCheckRepository).countBySource("frontend-debug");
        verify(integrationCheckRepository).count();
    }

    @Test
    void latestChecksReturnsRepositoryResult() {
        List<IntegrationCheck> expected = List.of(new IntegrationCheck("a"), new IntegrationCheck("b"));
        when(integrationCheckRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(expected);

        List<IntegrationCheck> actual = integrationDebugService.latestChecks();

        assertSame(expected, actual);
    }
}
