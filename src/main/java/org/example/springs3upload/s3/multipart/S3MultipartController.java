package org.example.springs3upload.s3.multipart;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
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
            @RequestPart("uploadFiles") List<MultipartFile> multipartFiles) {
        String directoryPath = "haeng-dong/s3-upload-test/"; // 원하는 디렉토리 경로

        for (MultipartFile file : multipartFiles) {
            try (InputStream inputStream = file.getInputStream()) {
                long contentLength = file.getSize(); // 파일 크기 가져오기
                System.out.println("contentLength = " + contentLength);
                // S3에 업로드 (key에 디렉토리 경로 포함)
                String key = directoryPath + file.getOriginalFilename() + UUID.randomUUID(); // 디렉토리 경로와 파일명 결합
                s3UploadService.uploadImageToS3("techcourse-project-2024", key, inputStream, contentLength);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "업로드 완료";
    }

    @PostMapping("/api/s3/multipart-files/webp")
    public String uploadMultipleFilesAsWebP(
            @RequestPart("uploadFiles") List<MultipartFile> multipartFiles) {
        String directoryPath = "haeng-dong/s3-upload-test/";

        for (MultipartFile file : multipartFiles) {
            try (InputStream inputStream = file.getInputStream()) {
                // 1. 업로드된 파일을 BufferedImage로 읽기
                BufferedImage originalImage = ImageIO.read(inputStream);

                // 2. WebP로 변환하기 위해 ByteArrayOutputStream 사용
                ByteArrayOutputStream os = new ByteArrayOutputStream();

                // 3. WebP Writer 가져오기
                ImageWriter writer = ImageIO.getImageWritersByFormatName("webp").next();
                ImageOutputStream ios = ImageIO.createImageOutputStream(os);
                writer.setOutput(ios);

                // 4. BufferedImage를 WebP로 변환하여 출력 스트림에 쓰기
                writer.write(originalImage);

                // 5. 리소스 해제
                ios.close();
                writer.dispose();

                // 6. 변환된 WebP 이미지를 S3에 업로드 (InputStream으로 변환)
                InputStream webpInputStream = new ByteArrayInputStream(os.toByteArray());
                long contentLength = os.size();

                // 7. 파일명 처리 (기존 확장자를 제거하고 ".webp" 추가)
                String originalFileName = file.getOriginalFilename();
                String fileNameWithoutExtension = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
                String key = directoryPath + fileNameWithoutExtension + UUID.randomUUID() + ".webp";

                // 8. S3에 업로드
                s3UploadService.uploadImageToS3("techcourse-project-2024", key, webpInputStream, contentLength);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "업로드 완료";
    }

//    public File convertToWebp(MultipartFile multipartFile) throws IOException {
//        // MultipartFile에서 BufferedImage로 읽기
//        BufferedImage image = ImageIO.read(multipartFile.getInputStream());
//
//        // webp 형식으로 변환할 파일 생성
//        File webpFile = new File("변환된_이미지.webp");
//
//        // ImageWriter 가져오기
//        ImageWriter writer = ImageIO.getImageWritersByFormatName("webp").next();
//
//        // 출력 스트림 설정
//        try (ImageOutputStream ios = ImageIO.createImageOutputStream(webpFile)) {
//            writer.setOutput(ios);
//
//            // 이미지 쓰기 파라미터 설정 (품질 조절 등)
//            ImageWriteParam param = writer.getDefaultWriteParam();
//            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
//            param.setCompressionQuality(0.8f); // 품질 설정 (0.0f ~ 1.0f)
//
//            // 이미지 쓰기
//            writer.write(null, new IIOImage(image, null, null), param);
//        } finally {
//            writer.dispose();
//        }
//
//        return webpFile;
//    }

    @PostMapping("/api/s3/multipart-files/future")
    public String uploadMultipleFilesFuture(
            @RequestPart("uploadFiles") List<MultipartFile> multipartFiles) {
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
            @RequestPart("uploadFiles") List<MultipartFile> multipartFiles) {
        s3UploadService.uploadImageToS3sAsync("techcourse-project-2024", multipartFiles);
        return "업로드 완료";
    }
}
