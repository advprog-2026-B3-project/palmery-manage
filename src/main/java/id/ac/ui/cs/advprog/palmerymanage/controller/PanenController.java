package id.ac.ui.cs.advprog.palmerymanage.controller;

import id.ac.ui.cs.advprog.palmerymanage.model.FotoPanen;
import id.ac.ui.cs.advprog.palmerymanage.service.PanenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@RestController
@RequestMapping("/api/panen")
public class PanenController {

    @Autowired
    private PanenService panenService;

    @PostMapping("/{hasilPanenId}/upload-foto")
    public ResponseEntity<?> uploadFoto(
            @PathVariable UUID hasilPanenId,
            @RequestParam("file") MultipartFile file) {
        try {
            FotoPanen savedFoto = panenService.uploadFotoBukti(hasilPanenId, file);
            return ResponseEntity.ok(savedFoto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Gagal: " + e.getMessage());
        }
    }
}