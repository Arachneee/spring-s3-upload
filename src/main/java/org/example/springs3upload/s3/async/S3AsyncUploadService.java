package org.example.springs3upload.s3.async;

import static software.amazon.awssdk.core.sync.RequestBody.fromInputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

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

            Thread thread = Thread.currentThread();
            s3Client.putObject(putObjectRequest, fromInputStream(inputStream, contentLength));
            log.info("Blocking Thead {}, {} 업로드 완료", thread.getName(), thread.getId());
        } catch (IOException e) {
            throw new RuntimeException("업로드 실패");
        }
    }

    private void uploadImageNonBlocking(MultipartFile image) {
        AtomicBoolean isClosed = new AtomicBoolean(false);  // InputStream이 닫혔는지 여부를 추적
        InputStream inputStream = null;

        try {
            inputStream = new BufferedInputStream(image.getInputStream());
            String fileName = UUID.randomUUID() + image.getOriginalFilename();
            String key = directoryPath + fileName;
            long contentLength = image.getSize();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentLength(contentLength)
                    .contentType(image.getContentType())
                    .build();

            InputStream finalInputStream = inputStream;

            CompletableFuture<PutObjectResponse> future = s3AsyncClient.putObject(
                    putObjectRequest,
                    AsyncRequestBody.fromInputStream(inputStream, contentLength, executorService)
            ).whenComplete((response, exception) -> {
                if (!isClosed.getAndSet(true)) {
                    try {
                        finalInputStream.close();
                        if (exception != null) {
                            log.error("Non Blocking 업로드 실패: {}", exception.getMessage());
                        } else {
                            log.info("Non Blocking 업로드 완료");
                        }
                    } catch (IOException e) {
                        log.error("InputStream 닫기 실패: {}", e.getMessage());
                    }
                }
            });

        } catch (IOException e) {
            log.error("업로드 실패: {}", e.getMessage());
            throw new RuntimeException("업로드 실패", e);
        }
    }
}
