package id.ac.ui.cs.advprog.palmerymanage.controller;

import id.ac.ui.cs.advprog.palmerymanage.service.RustfsService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/harvests/photos")
public class PhotoUploadController {

    private final RustfsService rustfsService;

    public PhotoUploadController(RustfsService rustfsService) {
        this.rustfsService = rustfsService;
    }

    @PostMapping
    public ResponseEntity<?> uploadPhoto(
            @RequestHeader("X-User-Role") String role,
            @RequestParam("file") MultipartFile file) {

        if (!"BURUH".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("Akses ditolak: hanya BURUH yang boleh upload foto.");
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File tidak boleh kosong.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body("File harus berupa gambar (jpg, png, dll).");
        }

        try {
            RustfsService.StoredFile storedFile = rustfsService.uploadFile(file);
            Map<String, Object> response = new HashMap<>();
            response.put("url", "/api/harvests/photos/" + storedFile.key());
            response.put("storageUrl", storedFile.publicUrl());
            response.put("filename", file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
            response.put("sizeBytes", file.getSize());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<?> readPhoto(@PathVariable("filename") String filename) {
        try {
            RustfsService.StoredObject storedObject = rustfsService.readFile(filename);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(storedObject.contentType()))
                    .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                    .body(storedObject.bytes());
        } catch (NoSuchKeyException exception) {
            return ResponseEntity.notFound().build();
        } catch (RuntimeException exception) {
            return ResponseEntity.internalServerError().body(exception.getMessage());
        }
    }
}
