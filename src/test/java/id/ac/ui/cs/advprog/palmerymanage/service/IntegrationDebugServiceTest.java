package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.model.IntegrationCheck;
import id.ac.ui.cs.advprog.palmerymanage.repository.IntegrationCheckRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntegrationDebugServiceTest {

    @Mock
    private IntegrationCheckRepository integrationCheckRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private IntegrationDebugService integrationDebugService;

    @Test
    void integrationStatus_dbUp_returnsStatusMap() {
        when(jdbcTemplate.queryForObject(eq("SELECT 1"), eq(Integer.class))).thenReturn(1);
        when(integrationCheckRepository.count()).thenReturn(5L);
        when(integrationCheckRepository.countBySource("frontend-debug")).thenReturn(3L);

        Map<String, Object> result = integrationDebugService.integrationStatus();

        assertEquals("manage", result.get("service"));
        assertEquals("up", result.get("backend"));
        assertEquals(5L, result.get("record_count"));
        assertEquals(3L, result.get("frontend_debug_count"));
        assertNotNull(result.get("timestamp"));

        @SuppressWarnings("unchecked")
        Map<String, Object> db = (Map<String, Object>) result.get("database");
        assertEquals("up", db.get("status"));
        assertEquals(1, db.get("ping"));
    }

    @Test
    void integrationStatus_dbPingNull_returnsUnknown() {
        when(jdbcTemplate.queryForObject(eq("SELECT 1"), eq(Integer.class))).thenReturn(null);
        when(integrationCheckRepository.count()).thenReturn(0L);
        when(integrationCheckRepository.countBySource("frontend-debug")).thenReturn(0L);

        Map<String, Object> result = integrationDebugService.integrationStatus();

        @SuppressWarnings("unchecked")
        Map<String, Object> db = (Map<String, Object>) result.get("database");
        assertEquals("unknown", db.get("status"));
    }

    @Test
    void createCheck_withSource_savesAndReturnsMap() {
        IntegrationCheck saved = new IntegrationCheck("my-source");
        org.springframework.test.util.ReflectionTestUtils.setField(saved, "id", 1L);
        saved.prePersist();

        when(integrationCheckRepository.save(any(IntegrationCheck.class))).thenReturn(saved);
        when(integrationCheckRepository.countBySource("my-source")).thenReturn(1L);
        when(integrationCheckRepository.count()).thenReturn(1L);

        Map<String, Object> result = integrationDebugService.createCheck("my-source");

        assertEquals(1L, result.get("id"));
        assertEquals("my-source", result.get("source"));
        assertEquals(1L, result.get("source_count"));
        assertEquals(1L, result.get("total_count"));
    }

    @Test
    void createCheck_nullSource_usesDefault() {
        IntegrationCheck saved = new IntegrationCheck("frontend-debug");
        org.springframework.test.util.ReflectionTestUtils.setField(saved, "id", 2L);
        saved.prePersist();

        when(integrationCheckRepository.save(any(IntegrationCheck.class))).thenReturn(saved);
        when(integrationCheckRepository.countBySource("frontend-debug")).thenReturn(1L);
        when(integrationCheckRepository.count()).thenReturn(1L);

        Map<String, Object> result = integrationDebugService.createCheck(null);

        assertEquals("frontend-debug", result.get("source"));
    }

    @Test
    void createCheck_blankSource_usesDefault() {
        IntegrationCheck saved = new IntegrationCheck("frontend-debug");
        org.springframework.test.util.ReflectionTestUtils.setField(saved, "id", 3L);
        saved.prePersist();

        when(integrationCheckRepository.save(any(IntegrationCheck.class))).thenReturn(saved);
        when(integrationCheckRepository.countBySource("frontend-debug")).thenReturn(1L);
        when(integrationCheckRepository.count()).thenReturn(1L);

        Map<String, Object> result = integrationDebugService.createCheck("   ");

        assertEquals("frontend-debug", result.get("source"));
    }

    @Test
    void latestChecks_returnsList() {
        IntegrationCheck check = new IntegrationCheck("test");
        when(integrationCheckRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(List.of(check));

        List<IntegrationCheck> result = integrationDebugService.latestChecks();

        assertEquals(1, result.size());
        verify(integrationCheckRepository).findTop10ByOrderByCreatedAtDesc();
    }
}
