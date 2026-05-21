package id.ac.ui.cs.advprog.palmerymanage.pengiriman;

import id.ac.ui.cs.advprog.palmerymanage.model.Pengiriman;
import id.ac.ui.cs.advprog.palmerymanage.model.PengirimanStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PengirimanResponseMapperTest {

    private PengirimanResponseMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PengirimanResponseMapper();
    }

    @Test
    void mapsAllFieldsToResponseMap() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2026-05-20T10:00:00Z");

        Pengiriman pengiriman = new Pengiriman();
        pengiriman.setId(id);
        pengiriman.setSupirId("supir-1");
        pengiriman.setMandorId("mandor-1");
        pengiriman.setKebunId("kebun-1");
        pengiriman.setTotalKg(120);
        pengiriman.setStatus(PengirimanStatus.PENDING_ADMIN_REVIEW);
        pengiriman.setPanenIds(List.of("panen-1"));
        pengiriman.setRejectedReason("test");
        pengiriman.setRecognizedKg(100);
        pengiriman.setCreatedAt(now);
        pengiriman.setUpdatedAt(now);

        Map<String, Object> response = mapper.toResponse(pengiriman);

        assertEquals(id, response.get("id"));
        assertEquals("supir-1", response.get("supir_id"));
        assertEquals("mandor-1", response.get("mandor_id"));
        assertEquals("kebun-1", response.get("kebun_id"));
        assertEquals(120, response.get("total_kg"));
        assertEquals("PENDING_ADMIN_REVIEW", response.get("status"));
        assertEquals(List.of("panen-1"), response.get("panen_ids"));
        assertEquals("test", response.get("rejected_reason"));
        assertEquals(100, response.get("recognized_kg"));
        assertEquals(now, response.get("created_at"));
        assertEquals(now, response.get("updated_at"));
    }
}
