package id.ac.ui.cs.advprog.palmerymanage.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.util.UUID;

@Service
public class RustfsService {

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

    //Upload file ke Rustfs & return URL publik
    public String uploadFile(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename() != null
                    ? file.getOriginalFilename()
                    : "file";

            String filename = UUID.randomUUID() + "_" + originalFilename;

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(filename)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return publicUrl + "/" + bucket + "/" + filename;

        } catch (Exception e) {
            throw new RuntimeException("Gagal upload foto ke Rustfs: " + e.getMessage(), e);
        }
    }
}