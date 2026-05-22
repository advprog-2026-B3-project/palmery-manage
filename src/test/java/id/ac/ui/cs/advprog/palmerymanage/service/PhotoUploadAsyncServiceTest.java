package id.ac.ui.cs.advprog.palmerymanage.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.core.sync.RequestBody;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhotoUploadAsyncServiceTest {

    @Test
    void uploadFileAsync_success() throws Exception {
        PhotoUploadAsyncService service = new PhotoUploadAsyncService(
                "http://localhost:9000", "minioadmin", "minioadmin", "test-bucket"
        );

        S3Client mockS3 = mock(S3Client.class);
        when(mockS3.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());
        ReflectionTestUtils.setField(service, "s3Client", mockS3);

        CompletableFuture<Void> result = service.uploadFileAsync("hello".getBytes(), "test.jpg", "image/jpeg");

        assertNotNull(result);
        assertNull(result.get());
        verify(mockS3).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadFileAsync_exceptionHandled() throws Exception {
        PhotoUploadAsyncService service = new PhotoUploadAsyncService(
                "http://localhost:9000", "minioadmin", "minioadmin", "test-bucket"
        );

        S3Client mockS3 = mock(S3Client.class);
        when(mockS3.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(new RuntimeException("S3 is down"));
        ReflectionTestUtils.setField(service, "s3Client", mockS3);

        CompletableFuture<Void> result = service.uploadFileAsync("hello".getBytes(), "test.jpg", "image/jpeg");

        assertNotNull(result);
        assertNull(result.get());
    }
}
