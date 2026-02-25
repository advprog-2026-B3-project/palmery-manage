package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.model.FotoPanen;
import id.ac.ui.cs.advprog.palmerymanage.model.HasilPanen;
import id.ac.ui.cs.advprog.palmerymanage.repository.FotoPanenRepository;
import id.ac.ui.cs.advprog.palmerymanage.repository.HasilPanenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PanenServiceTest {

    @Mock
    private HasilPanenRepository hasilPanenRepository;

    @Mock
    private FotoPanenRepository fotoPanenRepository;

    @InjectMocks
    private PanenService panenService;

    private UUID panenId;
    private HasilPanen mockPanen;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        panenId = UUID.randomUUID();
        mockPanen = new HasilPanen();
        mockPanen.setId(panenId);
        mockPanen.setTanggalPanen(LocalDate.now());
        mockPanen.setKgDipanen(new BigDecimal("150.50"));

        // Membuat file palsu untuk dites
        mockFile = new MockMultipartFile(
                "file",
                "bukti-panen.jpg",
                "image/jpeg",
                "gambar dummy".getBytes()
        );
    }

    @Test
    void testUploadFotoBukti_Success() throws Exception {
        // Aturan Mock: Kalau cari ID ini, kembalikan mockPanen
        when(hasilPanenRepository.findById(panenId)).thenReturn(Optional.of(mockPanen));

        // Aturan Mock: Kalau save ke DB, kembalikan file yang disimpan
        when(fotoPanenRepository.save(any(FotoPanen.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Eksekusi fungsi yang mau dites
        FotoPanen result = panenService.uploadFotoBukti(panenId, mockFile);

        // Validasi hasilnya
        assertNotNull(result);
        assertEquals("bukti-panen.jpg", result.getFilename());
        assertTrue(result.getUrl().contains("bukti-panen.jpg"));
        assertEquals(mockPanen, result.getHasilPanen());

        // Pastikan repository.save() dipanggil tepat 1 kali
        verify(fotoPanenRepository, times(1)).save(any(FotoPanen.class));
    }

    @Test
    void testUploadFotoBukti_PanenNotFound() {
        // Aturan Mock: Kalau cari ID, kembalikan kosong (tidak ketemu)
        when(hasilPanenRepository.findById(panenId)).thenReturn(Optional.empty());

        // Eksekusi dan validasi bahwa error terlempar
        Exception exception = assertThrows(Exception.class, () -> {
            panenService.uploadFotoBukti(panenId, mockFile);
        });

        assertEquals("Data Panen tidak ditemukan!", exception.getMessage());

        // Pastikan save ke DB tidak pernah dipanggil karena error duluan
        verify(fotoPanenRepository, never()).save(any(FotoPanen.class));
    }
}