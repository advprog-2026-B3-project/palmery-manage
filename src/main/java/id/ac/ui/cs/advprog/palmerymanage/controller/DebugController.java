package id.ac.ui.cs.advprog.palmerymanage.controller;

import id.ac.ui.cs.advprog.palmerymanage.dto.DebugCheckRequest;
import id.ac.ui.cs.advprog.palmerymanage.model.IntegrationCheck;
import id.ac.ui.cs.advprog.palmerymanage.service.IntegrationDebugService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/debug")
public class DebugController {

    private final IntegrationDebugService integrationDebugService;

    public DebugController(IntegrationDebugService integrationDebugService) {
        this.integrationDebugService = integrationDebugService;
    }

    @GetMapping("/healthcheck")
    public ResponseEntity<Map<String, Object>> healthcheck() {
        return ResponseEntity.ok(integrationDebugService.integrationStatus());
    }

    @GetMapping("/integration")
    public ResponseEntity<Map<String, Object>> integration() {
        return ResponseEntity.ok(integrationDebugService.integrationStatus());
    }

    @PostMapping("/checks")
    public ResponseEntity<Map<String, Object>> createCheck(@RequestBody(required = false) DebugCheckRequest request) {
        String source = request == null ? null : request.getSource();
        return ResponseEntity.status(HttpStatus.CREATED).body(integrationDebugService.createCheck(source));
    }

    @GetMapping("/checks")
    public ResponseEntity<List<Map<String, Object>>> latestChecks() {
        List<Map<String, Object>> response = integrationDebugService.latestChecks().stream()
                .map(this::toResponseEntry)
                .toList();
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> toResponseEntry(IntegrationCheck check) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("id", check.getId());
        entry.put("source", check.getSource());
        entry.put("created_at", check.getCreatedAt());
        return entry;
    }
}
