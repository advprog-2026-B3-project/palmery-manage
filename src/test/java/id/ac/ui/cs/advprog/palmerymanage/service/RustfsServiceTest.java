package id.ac.ui.cs.advprog.palmerymanage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RustfsServiceTest {

    private RustfsService rustfsService;

    @Mock
    private S3Client s3Client;

    @BeforeEach
    void setUp() {
        rustfsService = new RustfsService(
                "http://localhost:9000",
                "mock-access",
                "mock-secret",
                "test-bucket",
                "http://public-url.com"
        );
        
        ReflectionTestUtils.setField(rustfsService, "s3Client", s3Client);
    }

    @Test
    void testUploadFile_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test-image.jpg", "image/jpeg", "dummy data".getBytes()
        );

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String resultUrl = rustfsService.uploadFile(file);

        assertTrue(resultUrl.startsWith("http://public-url.com/test-bucket/"));
        assertTrue(resultUrl.endsWith("_test-image.jpg"));
        
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testUploadFile_NullOriginalFilename() throws IOException {
        org.springframework.web.multipart.MultipartFile file = mock(org.springframework.web.multipart.MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(null);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getInputStream()).thenReturn(new java.io.ByteArrayInputStream("dummy data".getBytes()));
        when(file.getSize()).thenReturn(10L);

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String resultUrl = rustfsService.uploadFile(file);

        assertTrue(resultUrl.startsWith("http://public-url.com/test-bucket/"));
        assertTrue(resultUrl.endsWith("_file"));

        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testUploadFile_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "dummy data".getBytes()
        );

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(new RuntimeException("S3 Connection Error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            rustfsService.uploadFile(file);
        });

        assertTrue(exception.getMessage().contains("Gagal upload foto ke Rustfs"));
        assertTrue(exception.getCause().getMessage().contains("S3 Connection Error"));
    }
}
