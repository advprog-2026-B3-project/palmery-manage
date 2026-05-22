package id.ac.ui.cs.advprog.palmerymanage.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.net.URI;

/**
 * Streaming proxy untuk foto bukti panen.
 * Backend pakai kredensial S3, browser tidak menghit MinIO langsung.
 */
@RestController
@RequestMapping("/api/harvests/photos")
public class PhotoProxyController {

    private static final Logger log = LoggerFactory.getLogger(PhotoProxyController.class);

    private final S3Client s3Client;
    private final String bucket;

    public PhotoProxyController(
            @Value("${rustfs.endpoint}") String endpoint,
            @Value("${rustfs.access-key}") String accessKey,
            @Value("${rustfs.secret-key}") String secretKey,
            @Value("${rustfs.bucket}") String bucket) {
        this.bucket = bucket;
        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.US_EAST_1)
                .forcePathStyle(true)
                .build();
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<byte[]> getPhoto(@PathVariable("filename") String filename) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(filename)
                    .build();

            try (ResponseInputStream<GetObjectResponse> stream = s3Client.getObject(request)) {
                byte[] body = stream.readAllBytes();
                String contentType = stream.response().contentType();
                if (contentType == null || contentType.isBlank()) {
                    contentType = guessContentType(filename);
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(contentType));
                headers.setCacheControl("public, max-age=86400");
                headers.setContentLength(body.length);

                return ResponseEntity.ok().headers(headers).body(body);
            }
        } catch (NoSuchKeyException ex) {
            log.warn("Photo not found: {}", filename);
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            log.error("Failed to fetch photo {}: {}", filename, ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    private String guessContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".bmp")) return "image/bmp";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        return "image/jpeg";
    }
}
