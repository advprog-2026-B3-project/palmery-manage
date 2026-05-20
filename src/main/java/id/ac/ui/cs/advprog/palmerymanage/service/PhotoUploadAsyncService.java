package id.ac.ui.cs.advprog.palmerymanage.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

@Service
public class PhotoUploadAsyncService {

    private static final Logger logger = LoggerFactory.getLogger(PhotoUploadAsyncService.class);

    private final S3Client s3Client;
    private final String bucket;

    public PhotoUploadAsyncService(
            @Value("${rustfs.endpoint}") String endpoint,
            @Value("${rustfs.access-key}") String accessKey,
            @Value("${rustfs.secret-key}") String secretKey,
            @Value("${rustfs.bucket}") String bucket
    ) {
        this.bucket = bucket;
        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.US_EAST_1)
                .forcePathStyle(true)
                .build();
    }

    @Async("asyncExecutor")
    public CompletableFuture<Void> uploadFileAsync(byte[] fileData, String filename, String contentType) {
        try {
            logger.info("Async upload started: {}", filename);

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(filename)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(fileData));

            logger.info("Async upload completed: {}", filename);
        } catch (Exception e) {
            logger.error("Async upload failed for {}: {}", filename, e.getMessage(), e);
        }
        return CompletableFuture.completedFuture(null);
    }
}
