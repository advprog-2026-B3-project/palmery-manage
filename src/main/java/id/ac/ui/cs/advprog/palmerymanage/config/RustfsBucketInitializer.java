package id.ac.ui.cs.advprog.palmerymanage.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutBucketPolicyRequest;

import java.net.URI;

/**
 * Memastikan bucket RustFS/MinIO untuk foto panen tersedia dan bisa dibaca publik.
 * Aman dijalankan berulang (idempotent).
 */
@Component
public class RustfsBucketInitializer {

    private static final Logger log = LoggerFactory.getLogger(RustfsBucketInitializer.class);

    private final String endpoint;
    private final String accessKey;
    private final String secretKey;
    private final String bucket;

    public RustfsBucketInitializer(
            @Value("${rustfs.endpoint}") String endpoint,
            @Value("${rustfs.access-key}") String accessKey,
            @Value("${rustfs.secret-key}") String secretKey,
            @Value("${rustfs.bucket}") String bucket) {
        this.endpoint = endpoint;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucket = bucket;
    }

    @PostConstruct
    public void ensureBucket() {
        try (S3Client s3 = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.US_EAST_1)
                .forcePathStyle(true)
                .build()) {

            createIfMissing(s3);
            applyPublicReadPolicy(s3);
        } catch (Exception ex) {
            log.warn("RustFS bucket init skipped (broker mungkin belum siap): {}", ex.getMessage());
        }
    }

    private void createIfMissing(S3Client s3) {
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            log.info("RustFS bucket '{}' sudah ada", bucket);
        } catch (NoSuchBucketException ex) {
            s3.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            log.info("RustFS bucket '{}' baru saja dibuat", bucket);
        } catch (software.amazon.awssdk.services.s3.model.S3Exception ex) {
            if (ex.statusCode() == 404) {
                s3.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
                log.info("RustFS bucket '{}' baru saja dibuat (via 404)", bucket);
            } else {
                throw ex;
            }
        }
    }

    private void applyPublicReadPolicy(S3Client s3) {
        String policy = """
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": {"AWS": ["*"]},
                      "Action": ["s3:GetObject"],
                      "Resource": ["arn:aws:s3:::%s/*"]
                    }
                  ]
                }
                """.formatted(bucket);

        s3.putBucketPolicy(PutBucketPolicyRequest.builder()
                .bucket(bucket)
                .policy(policy)
                .build());
        log.info("RustFS bucket '{}' diset public-read", bucket);
    }
}
