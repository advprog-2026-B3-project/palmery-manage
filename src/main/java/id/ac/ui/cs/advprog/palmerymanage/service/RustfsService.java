package id.ac.ui.cs.advprog.palmerymanage.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class RustfsService {

    public record StoredFile(String key, String publicUrl) {}

    public record StoredObject(byte[] bytes, String contentType) {}

    private final S3Client s3Client;
    private final String bucket;
    private final String publicUrl;

    public RustfsService(
            @Value("${rustfs.endpoint}") String endpoint,
            @Value("${rustfs.access-key}") String accessKey,
            @Value("${rustfs.secret-key}") String secretKey,
            @Value("${rustfs.bucket}") String bucket,
            @Value("${rustfs.public-url}") String publicUrl
    ) {
        this.bucket = bucket;
        this.publicUrl = publicUrl;

        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.US_EAST_1)
                .forcePathStyle(true)
                .build();
    }

    // Upload file ke bucket dan return key + URL publik bucket path.
    public StoredFile uploadFile(MultipartFile file) {
        try {
            String filename = buildObjectKey(file.getOriginalFilename());
            String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
            putObject(filename, contentType, file.getBytes());
            return new StoredFile(filename, buildPublicUrl(filename));
        } catch (IOException e) {
            throw new RuntimeException("Gagal upload foto ke Rustfs: " + e.getMessage(), e);
        }
    }

    public StoredObject readFile(String key) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(request);
            String contentType = objectBytes.response().contentType();
            return new StoredObject(objectBytes.asByteArray(), contentType == null ? "application/octet-stream" : contentType);
        } catch (NoSuchKeyException exception) {
            throw exception;
        } catch (Exception e) {
            throw new RuntimeException("Gagal membaca file dari Rustfs: " + e.getMessage(), e);
        }
    }

    public String buildPublicUrl(String key) {
        return stripTrailingSlash(publicUrl) + "/" + bucket + "/" + key;
    }

    private void putObject(String key, String contentType, byte[] fileData) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(fileData));
    }

    private String buildObjectKey(String originalFilename) {
        String safeName = originalFilename == null || originalFilename.isBlank()
                ? "file"
                : Paths.get(originalFilename).getFileName().toString().replaceAll("[^A-Za-z0-9._-]", "_");
        return UUID.randomUUID() + "_" + safeName;
    }

    private String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
