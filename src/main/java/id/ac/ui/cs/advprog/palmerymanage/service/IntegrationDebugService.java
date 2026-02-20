package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.model.IntegrationCheck;
import id.ac.ui.cs.advprog.palmerymanage.repository.IntegrationCheckRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IntegrationDebugService {

    private final IntegrationCheckRepository integrationCheckRepository;
    private final JdbcTemplate jdbcTemplate;

    public IntegrationDebugService(
            IntegrationCheckRepository integrationCheckRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.integrationCheckRepository = integrationCheckRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> integrationStatus() {
        long startedAt = System.nanoTime();
        Integer ping = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        long dbLatencyMs = (System.nanoTime() - startedAt) / 1_000_000;

        Map<String, Object> database = new HashMap<>();
        database.put("status", ping != null && ping == 1 ? "up" : "unknown");
        database.put("ping", ping);
        database.put("latency_ms", dbLatencyMs);

        Map<String, Object> response = new HashMap<>();
        response.put("service", "manage");
        response.put("backend", "up");
        response.put("database", database);
        response.put("record_count", integrationCheckRepository.count());
        response.put("frontend_debug_count", integrationCheckRepository.countBySource("frontend-debug"));
        response.put("timestamp", Instant.now().toString());
        return response;
    }

    public Map<String, Object> createCheck(String source) {
        String normalizedSource = source == null || source.isBlank() ? "frontend-debug" : source;
        IntegrationCheck saved = integrationCheckRepository.save(new IntegrationCheck(normalizedSource));

        Map<String, Object> response = new HashMap<>();
        response.put("id", saved.getId());
        response.put("source", saved.getSource());
        response.put("created_at", saved.getCreatedAt());
        response.put("source_count", integrationCheckRepository.countBySource(saved.getSource()));
        response.put("total_count", integrationCheckRepository.count());
        return response;
    }

    public List<IntegrationCheck> latestChecks() {
        return integrationCheckRepository.findTop10ByOrderByCreatedAtDesc();
    }
}
