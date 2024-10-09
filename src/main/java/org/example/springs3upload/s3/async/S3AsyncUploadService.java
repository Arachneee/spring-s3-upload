package org.example.springs3upload.s3.async;

import static software.amazon.awssdk.core.sync.RequestBody.fromInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.async.AsyncRequestBody;
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
    private final ExecutorService executorService;

    public void uploadSyncBlocking(List<MultipartFile> images) {
        images.forEach(this::uploadImageBlocking);
    }

    public void uploadSyncNonBlocking(List<MultipartFile> images) {
        images.forEach(this::uploadImageNonBlocking);

    }

    public void uploadParallelBlocking(List<MultipartFile> images) {
        images.stream()
                .parallel()
                .forEach(this::uploadImageBlocking);
    }

    public void uploadParallelNonBlocking(List<MultipartFile> images) {
        images.stream()
                .parallel()
                .forEach(this::uploadImageNonBlocking);
    }

    public void uploadAsyncBlocking(List<MultipartFile> images) {
        List<CompletableFuture<Void>> futures = images.stream()
                .map(image -> CompletableFuture.runAsync(() -> uploadImageBlocking(image), executorService))
                .toList();

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException e) {
            throw (RuntimeException) e.getCause();
        }
    }

    public void uploadAsyncNonBlocking(List<MultipartFile> images) {
        List<CompletableFuture<Void>> futures = images.stream()
                .map(image -> CompletableFuture.runAsync(() -> uploadImageNonBlocking(image), executorService))
                .toList();

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException e) {
            throw (RuntimeException) e.getCause();
        }
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

    private void uploadImageNonBlocking(MultipartFile image) {
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

            s3AsyncClient.putObject(putObjectRequest, AsyncRequestBody.fromInputStream(inputStream, contentLength, executorService));
        } catch (IOException e) {
            throw new RuntimeException("업로드 실패");
        }
    }
}
