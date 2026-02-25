package id.ac.ui.cs.advprog.palmerymanage.controller;

import id.ac.ui.cs.advprog.palmerymanage.model.FotoPanen;
import id.ac.ui.cs.advprog.palmerymanage.service.PanenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PanenController.class)
public class PanenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PanenService panenService;

    @Test
    void testUploadFoto_Success() throws Exception {
        UUID panenId = UUID.randomUUID();
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "sawit.jpg",
                "image/jpeg",
                "isi gambar".getBytes()
        );

        FotoPanen mockFoto = new FotoPanen();
        mockFoto.setId(UUID.randomUUID());
        mockFoto.setFilename("sawit.jpg");
        mockFoto.setUrl("https://rustfs.palmery.my.id/uploads/sawit.jpg");

        // Aturan Mock: Pura-pura service berhasil memproses foto
        when(panenService.uploadFotoBukti(eq(panenId), any())).thenReturn(mockFoto);

        // Simulasi HTTP POST request dengan file
        mockMvc.perform(multipart("/api/panen/" + panenId + "/upload-foto")
                        .file(mockFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").value("sawit.jpg"))
                .andExpect(jsonPath("$.url").value("https://rustfs.palmery.my.id/uploads/sawit.jpg"));
    }

    @Test
    void testUploadFoto_Failed() throws Exception {
        UUID panenId = UUID.randomUUID();
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "rusak.jpg",
                "image/jpeg",
                "isi gambar".getBytes()
        );

        // Aturan Mock: Pura-pura service melempar error
        when(panenService.uploadFotoBukti(eq(panenId), any()))
                .thenThrow(new Exception("Data Panen tidak ditemukan!"));

        // Simulasi HTTP POST request
        mockMvc.perform(multipart("/api/panen/" + panenId + "/upload-foto")
                        .file(mockFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Gagal: Data Panen tidak ditemukan!"));
    }
}