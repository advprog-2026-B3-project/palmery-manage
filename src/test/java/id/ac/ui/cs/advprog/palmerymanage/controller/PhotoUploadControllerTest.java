package id.ac.ui.cs.advprog.palmerymanage.controller;

import id.ac.ui.cs.advprog.palmerymanage.service.PhotoUploadAsyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import id.ac.ui.cs.advprog.palmerymanage.config.DevSecurityConfig;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PhotoUploadController.class)
@ActiveProfiles("dev")
@Import(DevSecurityConfig.class)
@TestPropertySource(properties = {
        "rustfs.public-url=http://mock-rustfs.com",
        "rustfs.bucket=test-bucket"
})
class PhotoUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PhotoUploadAsyncService photoUploadAsyncService;

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
        when(photoUploadAsyncService.uploadFileAsync(any(byte[].class), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(multipart("/api/harvests/photos")
                        .file(validFile)
                        .header("X-User-Role", "BURUH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").exists())
                .andExpect(jsonPath("$.filename").value("test.jpg"))
                .andExpect(jsonPath("$.sizeBytes").value(validFile.getSize()));

        verify(photoUploadAsyncService).uploadFileAsync(any(byte[].class), anyString(), anyString());
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
        when(photoUploadAsyncService.uploadFileAsync(any(byte[].class), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(multipart("/api/harvests/photos")
                        .file(nullNameFile)
                        .header("X-User-Role", "BURUH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").value(""));
    }

    @Test
    void testUploadPhoto_IOException() throws Exception {
        MockMultipartFile ioExceptionFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "dummy".getBytes()) {
            @Override
            public byte[] getBytes() throws java.io.IOException {
                throw new java.io.IOException("Simulated IO Exception");
            }
        };

        mockMvc.perform(multipart("/api/harvests/photos")
                        .file(ioExceptionFile)
                        .header("X-User-Role", "BURUH"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Gagal membaca file: Simulated IO Exception"));
    }
}
