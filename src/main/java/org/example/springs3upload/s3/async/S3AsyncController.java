package org.example.springs3upload.s3.async;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Controller
public class S3AsyncController {

    private final S3AsyncUploadService s3AsyncUploadService;

    @PostMapping("/sync/blocking")
    public void uploadSyncBlocking(@RequestPart("images") List<MultipartFile> images) {
        s3AsyncUploadService.uploadSyncBlocking(images);
    }

    @PostMapping("/async/blocking")
    public void uploadAsyncBlocking(@RequestPart("images") List<MultipartFile> images) {
        s3AsyncUploadService.uploadAsyncBlocking(images);
    }

    @PostMapping("/sync/non-blocking")
    public void uploadSyncNonBlocking(@RequestPart("images") List<MultipartFile> images) {
        s3AsyncUploadService.uploadSyncNonBlocking(images);
    }

    @PostMapping("/async/non-blocking")
    public void uploadAsyncNonBlocking(@RequestPart("images") List<MultipartFile> images) {
        s3AsyncUploadService.uploadAsyncNonBlocking(images);
    }
}
