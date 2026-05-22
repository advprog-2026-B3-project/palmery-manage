package id.ac.ui.cs.advprog.palmerymanage.controller;

import id.ac.ui.cs.advprog.palmerymanage.service.RustfsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import id.ac.ui.cs.advprog.palmerymanage.config.DevSecurityConfig;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PhotoUploadController.class)
@ActiveProfiles("dev")
@Import(DevSecurityConfig.class)
class PhotoUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RustfsService rustfsService;

    private MockMultipartFile validFile;
    private MockMultipartFile emptyFile;
    private MockMultipartFile nonImageFile;

    @BeforeEach
    void setUp() {
        validFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "dummy image content".getBytes());
        emptyFile = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);
        nonImageFile = new MockMultipartFile("file", "test.txt", "text/plain", "dummy text".getBytes());
    }

    @Test
    void testUploadPhoto_Success() throws Exception {
        when(rustfsService.uploadFile(any()))
                .thenReturn(new RustfsService.StoredFile(
                        "generated_test.jpg",
                        "https://api-manage.palmery.my.id/assets/test-bucket/generated_test.jpg"
                ));

        mockMvc.perform(multipart("/api/harvests/photos")
                        .file(validFile)
                        .header("X-User-Role", "BURUH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("/api/harvests/photos/generated_test.jpg"))
                .andExpect(jsonPath("$.storageUrl").value("https://api-manage.palmery.my.id/assets/test-bucket/generated_test.jpg"))
                .andExpect(jsonPath("$.filename").value("test.jpg"))
                .andExpect(jsonPath("$.sizeBytes").value(validFile.getSize()));

        verify(rustfsService).uploadFile(any());
    }

    @Test
    void testUploadPhoto_RoleNotBuruh() throws Exception {
        mockMvc.perform(multipart("/api/harvests/photos")
                        .file(validFile)
                        .header("X-User-Role", "MANDOR"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Akses ditolak: hanya BURUH yang boleh upload foto."));
    }

    @Test
    void testUploadPhoto_EmptyFile() throws Exception {
        mockMvc.perform(multipart("/api/harvests/photos")
                        .file(emptyFile)
                        .header("X-User-Role", "BURUH"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File tidak boleh kosong."));
    }

    @Test
    void testUploadPhoto_NonImageFile() throws Exception {
        mockMvc.perform(multipart("/api/harvests/photos")
                        .file(nonImageFile)
                        .header("X-User-Role", "BURUH"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File harus berupa gambar (jpg, png, dll)."));
    }

    @Test
    void testUploadPhoto_NullContentType() throws Exception {
        MockMultipartFile nullContentFile = new MockMultipartFile("file", "test.jpg", null, "dummy".getBytes());
        mockMvc.perform(multipart("/api/harvests/photos")
                        .file(nullContentFile)
                        .header("X-User-Role", "BURUH"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File harus berupa gambar (jpg, png, dll)."));
    }

    @Test
    void testUploadPhoto_NullOriginalFilename() throws Exception {
        MockMultipartFile nullNameFile = new MockMultipartFile("file", (String) null, "image/jpeg", "dummy image content".getBytes());
        when(rustfsService.uploadFile(any()))
                .thenReturn(new RustfsService.StoredFile(
                        "generated_file",
                        "https://api-manage.palmery.my.id/assets/test-bucket/generated_file"
                ));

        mockMvc.perform(multipart("/api/harvests/photos")
                        .file(nullNameFile)
                        .header("X-User-Role", "BURUH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").value("file"));
    }

    @Test
    void testUploadPhoto_UploadFailure() throws Exception {
        when(rustfsService.uploadFile(any())).thenThrow(new RuntimeException("Gagal upload foto ke Rustfs: Simulated failure"));

        mockMvc.perform(multipart("/api/harvests/photos")
                        .file(validFile)
                        .header("X-User-Role", "BURUH"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Gagal upload foto ke Rustfs: Simulated failure"));
    }

    @Test
    void testReadPhoto_Success() throws Exception {
        when(rustfsService.readFile("generated_test.jpg"))
                .thenReturn(new RustfsService.StoredObject("image-bytes".getBytes(), "image/jpeg"));

        mockMvc.perform(get("/api/harvests/photos/generated_test.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"))
                .andExpect(content().bytes("image-bytes".getBytes()));
    }

    @Test
    void testReadPhoto_NotFound() throws Exception {
        when(rustfsService.readFile("missing.jpg"))
                .thenThrow(software.amazon.awssdk.services.s3.model.NoSuchKeyException.builder().message("missing").build());

        mockMvc.perform(get("/api/harvests/photos/missing.jpg"))
                .andExpect(status().isNotFound());
    }
}
