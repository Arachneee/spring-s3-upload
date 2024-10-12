package org.example.springs3upload.s3.async;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@RestController
public class S3AsyncController {

    private final S3AsyncUploadService s3AsyncUploadService;

    @PostMapping("/sync/blocking")
    public void uploadSyncBlocking(@RequestPart("images") List<MultipartFile> images) {
        s3AsyncUploadService.uploadSyncBlocking(images);
    }

    @PostMapping("/parallel/blocking")
    public void uploadParallelBlocking(@RequestPart("images") List<MultipartFile> images) {
        s3AsyncUploadService.uploadParallelBlocking(images);
    }

    @PostMapping("/async/blocking")
    public void uploadAsyncBlocking(@RequestPart("images") List<MultipartFile> images) {
        s3AsyncUploadService.uploadAsyncBlocking(images);
    }

    @PostMapping("/sync/non-blocking")
    public void uploadSyncNonBlocking(@RequestPart("images") List<MultipartFile> images) {
        s3AsyncUploadService.uploadSyncNonBlocking(images);
    }

    @PostMapping("/parallel/non-blocking")
    public void uploadParallelNonBlocking(@RequestPart("images") List<MultipartFile> images) {
        s3AsyncUploadService.uploadParallelNonBlocking(images);
    }

    @PostMapping("/async/non-blocking")
    public void uploadAsyncNonBlocking(@RequestPart("images") List<MultipartFile> images) {
        s3AsyncUploadService.uploadAsyncNonBlocking(images);
        log.info("전체 업로드 종료");
    }
}
