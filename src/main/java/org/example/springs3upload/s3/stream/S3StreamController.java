package org.example.springs3upload.s3.stream;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import org.example.springs3upload.s3.multipart.S3UploadService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class S3StreamController {

    private final S3UploadService s3InputStreamUploadService;

    public S3StreamController(S3UploadService s3InputStreamUploadService) {
        this.s3InputStreamUploadService = s3InputStreamUploadService;
    }

    @PostMapping("/api/s3/stream")
    public String uploadFileByStream(HttpServletRequest request) {
        try (InputStream inputStream = request.getInputStream()) {
            long contentLength = request.getContentLengthLong();

            String directoryPath = "haeng-dong/s3-upload-test/"; // 원하는 디렉토리 경로

            // 파일명을 헤더에서 받거나 직접 지정x
            String fileName = request.getHeader("file-name") + UUID.randomUUID();
            if (fileName == null || fileName.isEmpty()) {
                fileName = "default-file-name";
            }

            String key = directoryPath + fileName;

            // S3에 파일 업로드
            s3InputStreamUploadService.uploadImageToS3("techcourse-project-2024", key, inputStream, contentLength);

            return "업로드 완료";
        } catch (IOException e) {
            return "업로드 실패: " + e.getMessage();
        }
    }
}
