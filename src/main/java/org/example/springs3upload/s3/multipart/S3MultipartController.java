package org.example.springs3upload.s3.multipart;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class S3MultipartController {

    private final S3UploadService s3UploadService;

    public S3MultipartController(S3UploadService s3UploadService) {
        this.s3UploadService = s3UploadService;
    }

    @PostMapping("/api/s3/multipart-files")
    public String uploadMultipleFiles(
            @RequestPart("uploadFiles") List<MultipartFile> multipartFiles
    ) {
        String directoryPath = "haeng-dong/s3-upload-test/"; // 원하는 디렉토리 경로

        for (MultipartFile file : multipartFiles) {
            try (InputStream inputStream = file.getInputStream();
                 PipedOutputStream pipedOut = new PipedOutputStream()) {
                // 이미지를 100x100 크기로 리사이징하고 스트림에 저장
                PipedInputStream pipedIn = new PipedInputStream(pipedOut);
                CountingOutputStream countingOutputStream = new CountingOutputStream(pipedOut);

                Thumbnails.of(inputStream)
                        .outputFormat("webp")
                        .toOutputStream(countingOutputStream);
//                byte[] byteArray = pipedIn.toByteArray();
//                long contentLength = byteArray.length;
                String key = directoryPath + UUID.randomUUID() + "test.webp"; //file.getOriginalFilename(); // 디렉토리 경로와 파일명 결합
                s3UploadService.uploadImageToS3("techcourse-project-2024", key, pipedIn, countingOutputStream.getSize());
            } catch (IOException e) {
                e.printStackTrace();
                return "업로드 실패";
            }
        }
        return "업로드 완료";
    }

    // OutputStream을 확장하여 데이터 크기를 추적하는 클래스
    private static class CountingOutputStream extends OutputStream {
        private final OutputStream delegate;
        private long size = 0;

        public CountingOutputStream(OutputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public void write(int b) throws IOException {
            delegate.write(b);
            size++;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            delegate.write(b, off, len);
            size += len;
        }

        public long getSize() {
            return size;
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }

    @PostMapping("/api/s3/multipart-files/webp")
    public String uploadMultipleFilesWebp(@RequestPart("uploadFiles") List<MultipartFile> multipartFiles) {
        String directoryPath = "haeng-dong/s3-upload-test/";

        for (MultipartFile file : multipartFiles) {
            try (InputStream inputStream = file.getInputStream()) {
                // 이미지 WebP 변환
                InputStream webpInputStream = convertToWebP(inputStream);

                long contentLength = webpInputStream.available(); // 변환된 WebP 파일 크기 가져오기
                String key = directoryPath + UUID.randomUUID() + ".webp";

                // S3에 업로드
                s3UploadService.uploadImageToS3("techcourse-project-2024", key, webpInputStream, contentLength);
            } catch (IOException e) {
                e.printStackTrace();
                return "업로드 중 오류 발생";
            }
        }
        return "업로드 완료";
    }

    // WebP 변환 메소드
    private InputStream convertToWebP(InputStream inputStream) throws IOException {
        // 이미지 로드
        BufferedImage originalImage = ImageIO.read(inputStream);

        // WebP로 변환
        ByteArrayOutputStream webpOutputStream = new ByteArrayOutputStream();
        ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(webpOutputStream);
        ImageIO.write(originalImage, "webp", imageOutputStream);

        return new ByteArrayInputStream(webpOutputStream.toByteArray());
    }

    @PostMapping("/api/s3/multipart-files/future")
    public String uploadMultipleFilesFuture(
            @RequestPart("uploadFiles") List<MultipartFile> multipartFiles
    ) {
        String directoryPath = "haeng-dong/s3-upload-test/"; // 원하는 디렉토리 경로

        // 병렬 업로드를 위한 CompletableFuture 리스트 생성
        List<CompletableFuture<Void>> uploadFutures = multipartFiles.stream().map(file ->
                CompletableFuture.runAsync(() -> {
                    try (InputStream inputStream = file.getInputStream()) {
                        long contentLength = file.getSize(); // 파일 크기 가져오기
                        System.out.println("contentLength = " + contentLength);
                        // S3에 업로드 (key에 디렉토리 경로 포함)
                        String key = directoryPath + file.getOriginalFilename(); // 디렉토리 경로와 파일명 결합
                        s3UploadService.uploadImageToS3("techcourse-project-2024", key, inputStream, contentLength);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
        ).collect(Collectors.toList());

        // 모든 업로드가 완료될 때까지 기다림
        CompletableFuture.allOf(uploadFutures.toArray(new CompletableFuture[0])).join();

        return "업로드 완료";
    }

    @PostMapping("/api/s3/multipart-files/async")
    public String uploadMultipleFilesAsync(
            @RequestPart("uploadFiles") List<MultipartFile> multipartFiles
    ) {
        s3UploadService.uploadImageToS3sAsync("techcourse-project-2024", multipartFiles);
        return "업로드 완료";
    }
}
