package id.ac.ui.cs.advprog.palmerymanage.pengiriman;

import id.ac.ui.cs.advprog.palmerymanage.model.Pengiriman;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

// SRP: sole responsibility is mapping {@link Pengiriman} entities to API response maps.
@Component
public class PengirimanResponseMapper {

    public Map<String, Object> toResponse(Pengiriman pengiriman) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", pengiriman.getId());
        map.put("supir_id", pengiriman.getSupirId());
        map.put("mandor_id", pengiriman.getMandorId());
        map.put("kebun_id", pengiriman.getKebunId());
        map.put("total_kg", pengiriman.getTotalKg());
        map.put("status", pengiriman.getStatus().name());
        map.put("panen_ids", pengiriman.getPanenIds());
        map.put("rejected_reason", pengiriman.getRejectedReason());
        map.put("recognized_kg", pengiriman.getRecognizedKg());
        map.put("created_at", pengiriman.getCreatedAt());
        map.put("updated_at", pengiriman.getUpdatedAt());
        return map;
    }
}
