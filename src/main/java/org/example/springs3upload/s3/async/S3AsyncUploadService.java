package org.example.springs3upload.s3.async;

import static software.amazon.awssdk.core.sync.RequestBody.fromInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3AsyncUploadService {

    @Value("${image.bucket}")
    private String bucketName;

    @Value("${image.directory}")
    private String directoryPath;

    private final S3Client s3Client;
    private final S3AsyncClient s3AsyncClient;

    public void uploadSyncBlocking(List<MultipartFile> images) {
        images.forEach(this::uploadImageBlocking);
    }

    private void uploadImageBlocking(MultipartFile image) {
        try (InputStream inputStream = image.getInputStream()) {
            String fileName = UUID.randomUUID() + image.getOriginalFilename();
            String key = directoryPath + fileName;
            long contentLength = image.getSize();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentLength(contentLength)
                    .contentType(image.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, fromInputStream(inputStream, contentLength));
        } catch (IOException e) {
            throw new RuntimeException("업로드 실패");
        }
    }

    public void uploadparallerBlocking(List<MultipartFile> images) {
        images.stream()
                .parallel()
                .forEach(this::uploadImageBlocking);
    }

    public void uploadAsyncBlocking(List<MultipartFile> images) {

    }

    public void uploadSyncNonBlocking(List<MultipartFile> images) {

    }

    public void uploadAsyncNonBlocking(List<MultipartFile> images) {

    }
}
