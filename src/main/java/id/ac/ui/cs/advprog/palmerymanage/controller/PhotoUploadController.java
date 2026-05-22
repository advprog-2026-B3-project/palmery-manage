package id.ac.ui.cs.advprog.palmerymanage.controller;

import id.ac.ui.cs.advprog.palmerymanage.service.PhotoUploadAsyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/harvests/photos")
public class PhotoUploadController {

    private final PhotoUploadAsyncService photoUploadAsyncService;

    public PhotoUploadController(PhotoUploadAsyncService photoUploadAsyncService) {
        this.photoUploadAsyncService = photoUploadAsyncService;
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
            byte[] fileData = file.getBytes();
            String originalFilename = file.getOriginalFilename() != null
                    ? file.getOriginalFilename()
                    : "file";
            String filename = UUID.randomUUID() + "_" + originalFilename;

            String url = "/api/harvests/photos/" + filename;

            photoUploadAsyncService.uploadFileAsync(fileData, filename, contentType);

            Map<String, Object> response = new HashMap<>();
            response.put("url", url);
            response.put("filename", originalFilename);
            response.put("sizeBytes", file.getSize());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Gagal membaca file: " + e.getMessage());
        }
    }
}