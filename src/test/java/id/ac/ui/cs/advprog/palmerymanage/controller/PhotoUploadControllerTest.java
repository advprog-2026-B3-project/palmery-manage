package id.ac.ui.cs.advprog.palmerymanage.controller;

import id.ac.ui.cs.advprog.palmerymanage.service.RustfsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PhotoUploadController.class)
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
        when(rustfsService.uploadFile(any())).thenReturn("http://mock-url.com/bucket/test.jpg");

        mockMvc.perform(multipart("/api/harvests/photos")
                        .file(validFile)
                        .header("X-User-Role", "BURUH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("http://mock-url.com/bucket/test.jpg"))
                .andExpect(jsonPath("$.filename").value("test.jpg"));
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
    void testUploadPhoto_ServiceThrowsException() throws Exception {
        when(rustfsService.uploadFile(any())).thenThrow(new RuntimeException("S3 is down"));

        mockMvc.perform(multipart("/api/harvests/photos")
                        .file(validFile)
                        .header("X-User-Role", "BURUH"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Upload gagal: S3 is down"));
    }
}
