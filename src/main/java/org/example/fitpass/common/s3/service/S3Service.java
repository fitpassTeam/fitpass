package org.example.fitpass.common.s3.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Component
@Slf4j
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @PostConstruct
    public void initLog() {
        log.info("S3Service 초기화 완료, 버킷 이름: {}", bucketName);
    }

    @Transactional
    public List<String> uploadFiles(List<MultipartFile> files){
        List<String> imageUrls = new ArrayList<>();
        for(MultipartFile file : files){
            String imageUrl = uploadFileToS3(file);
            imageUrls.add(imageUrl);
        }
        return imageUrls;
    }

    @Transactional
    public String uploadFileToS3(MultipartFile file) {
        // 파일 확장자만 추출
        String originalFilename = file.getOriginalFilename();
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 고유한 파일 이름 생성
        String fileName = UUID.randomUUID().toString() + extension;

        try {
            // ObjectMetadata 설정
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize()); // 파일 크기 설정
            metadata.setContentType(file.getContentType()); // 파일 타입 설정 (선택 사항)

            // S3에 파일 업로드
            amazonS3.putObject(
                new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata)
            );

            // 업로드한 파일의 URL 반환
            return amazonS3.getUrl(bucketName, fileName).toString();
        } catch (IOException e) {
            throw new BaseException(ExceptionCode.S3_UPLOAD_FAIL);
        }
    }

    @Transactional
    public void deleteFileFromS3(String fileUrl) {
        try {
            // URL에서 파일 이름만 추출
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

            // URL 디코딩하여 파일 이름 처리 (특수 문자 및 공백 처리)
            fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);

            // S3에서 해당 파일 삭제
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, fileName));
            log.info("삭제 성공: {}", fileName);
        } catch (Exception e) {
            log.error("삭제 실패", e);
            throw new BaseException(ExceptionCode.S3_DELETE_FAIL);
        }
    }

    // 단일 파일 업로드 메서드
    public String uploadSingleFile(MultipartFile file) {
        return uploadFileToS3(file); // 내부 private 메서드 호출
    }

    public String generatePresignedUrl(String originalFilename, String contentType) {
        // 파일 확장자 검증
        if (!isAllowedFileType(originalFilename)) {
            throw new BaseException(ExceptionCode.INVALID_FILE_TYPE);
        }

        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID().toString() + extension;

        // URL 유효기간 설정 (예: 5분)
        Date expiration = new Date();
        expiration.setTime(expiration.getTime() + 1000 * 60 * 5);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
            new GeneratePresignedUrlRequest(bucketName, fileName)
                .withMethod(HttpMethod.PUT)
                .withExpiration(expiration)
                .withContentType(contentType);

        String presignedUrl = amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString();
        
        log.info("Presigned URL 생성 완료: fileName={}, contentType={}", fileName, contentType);
        return presignedUrl;
    }

    private boolean isAllowedFileType(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        
        String[] allowedExtensions = {".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp", ".tiff"};
        String lowerFilename = filename.toLowerCase();
        return Arrays.stream(allowedExtensions)
                .anyMatch(lowerFilename::endsWith);
    }

    public String extractFileNameFromUrl(String presignedUrl) {
        if (presignedUrl == null || !presignedUrl.contains("/")) {
            return "";
        }

        String path = presignedUrl.split("\\?")[0];
        int lastSlashIndex = path.lastIndexOf("/");

        if (lastSlashIndex == -1 || lastSlashIndex == path.length() - 1) {
            return "";
        }

        return path.substring(lastSlashIndex + 1);
    }

}
