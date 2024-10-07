package org.example.springs3upload.s3.multipart;

import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Component
public class S3UploadService {

    private final S3Client s3Client;

    public S3UploadService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void uploadImageToS3(String bucketName, String key, InputStream inputStream, long contentLength) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentLength(contentLength) // contentLength 추가
                .contentType("image/" + key.split("\\.")[1])
                .build();

        RequestBody requestBody = RequestBody.fromInputStream(inputStream, contentLength);
        s3Client.putObject(putObjectRequest, requestBody);
    }
}
