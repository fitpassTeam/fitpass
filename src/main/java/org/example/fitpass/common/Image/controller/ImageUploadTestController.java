package org.example.fitpass.common.Image.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.common.s3.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/test/images")
@Tag(name = "IMAGE UPLOAD TEST API", description = "기존 방식 vs Presigned URL 방식 비교 테스트용 API")
@Slf4j
public class ImageUploadTestController {

    private final S3Service s3Service;

    @Operation(
        summary = "🔴 [기존 방식] 서버를 통한 이미지 업로드",
        description = """
            기존 방식: 클라이언트 → 서버 → S3
            
            장점:
            - 구현이 간단함
            - 서버에서 파일 검증 가능
            - 업로드 진행률 추적 가능
            
            단점:
            - 서버 리소스 사용 (대역폭, 메모리)
            - 서버 부하 증가
            - 업로드 시간이 더 길어짐 (서버 경유)
            """
    )
    @PostMapping("/traditional")
    public ResponseEntity<ResponseMessage<Map<String, Object>>> traditionalUpload(
        @Parameter(description = "업로드할 이미지 파일", required = true)
        @RequestParam("image") MultipartFile file
    ) {
        long startTime = System.currentTimeMillis();
        
        log.info("🔴 [기존 방식] 이미지 업로드 시작 - 파일명: {}, 크기: {} bytes", 
                file.getOriginalFilename(), file.getSize());
        
        try {
            String uploadedImageUrl = s3Service.uploadSingleFile(file);
            long endTime = System.currentTimeMillis();
            long uploadTime = endTime - startTime;
            
            Map<String, Object> response = new HashMap<>();
            response.put("method", "TRADITIONAL");
            response.put("imageUrl", uploadedImageUrl);
            response.put("uploadTimeMs", uploadTime);
            response.put("fileSize", file.getSize());
            response.put("fileName", file.getOriginalFilename());
            response.put("description", "서버를 통한 업로드 완료");
            
            log.info("🔴 [기존 방식] 업로드 완료 - URL: {}, 소요시간: {}ms", 
                    uploadedImageUrl, uploadTime);
            
            ResponseMessage<Map<String, Object>> responseMessage =
                ResponseMessage.success(SuccessCode.S3_UPLOAD_SUCCESS, response);
            return ResponseEntity.ok(responseMessage);
            
        } catch (Exception e) {
            log.error("🔴 [기존 방식] 업로드 실패", e);
            throw e;
        }
    }

    @Operation(
        summary = "🟢 [신규 방식] Presigned URL을 이용한 이미지 업로드 준비",
        description = """
            신규 방식: 클라이언트 → S3 (직접)
            
            1단계: 이 API로 Presigned URL 받기
            2단계: 클라이언트가 받은 URL로 S3에 직접 업로드
            
            장점:
            - 서버 리소스 절약
            - 빠른 업로드 속도
            - 서버 부하 감소
            - 대용량 파일에 유리
            
            단점:
            - 구현이 복잡함
            - 클라이언트에서 에러 처리 필요
            - 업로드 진행률 추적이 어려움
            """
    )
    @PostMapping("/presigned-url-prepare")
    public ResponseEntity<ResponseMessage<Map<String, Object>>> presignedUrlPrepare(
        @Parameter(description = "업로드할 파일명 (확장자 포함)", required = true, example = "test-image.jpg")
        @RequestParam("filename") String filename,
        @Parameter(description = "파일의 Content-Type", required = true, example = "image/jpeg")
        @RequestParam("contentType") String contentType,
        @Parameter(description = "파일 크기 (바이트)", required = false, example = "1024000")
        @RequestParam(value = "fileSize", required = false, defaultValue = "0") long fileSize
    ) {
        long startTime = System.currentTimeMillis();
        
        log.info("🟢 [신규 방식] Presigned URL 생성 시작 - 파일명: {}, 타입: {}, 크기: {} bytes", 
                filename, contentType, fileSize);
        
        try {
            String presignedUrl = s3Service.generatePresignedUrl(filename, contentType);
            String fileName = s3Service.extractFileNameFromUrl(presignedUrl);
            long endTime = System.currentTimeMillis();
            long prepareTime = endTime - startTime;
            
            Map<String, Object> response = new HashMap<>();
            response.put("method", "PRESIGNED_URL");
            response.put("presignedUrl", presignedUrl);
            response.put("fileName", fileName);
            response.put("contentType", contentType);
            response.put("expiresIn", "300");
            response.put("prepareTimeMs", prepareTime);
            response.put("fileSize", fileSize);
            response.put("originalFileName", filename);
            response.put("uploadInstructions", Map.of(
                "step1", "이 API로 presignedUrl을 받았습니다",
                "step2", "클라이언트에서 받은 presignedUrl로 PUT 요청하여 S3에 직접 업로드하세요",
                "step3", "업로드 완료 후 fileName을 사용하여 최종 URL 구성: https://fit-pass-1.s3.ap-northeast-2.amazonaws.com/" + fileName
            ));
            
            log.info("🟢 [신규 방식] Presigned URL 생성 완료 - URL: {}, 소요시간: {}ms", 
                    presignedUrl, prepareTime);
            
            ResponseMessage<Map<String, Object>> responseMessage =
                ResponseMessage.success(SuccessCode.S3_PRESIGNED_URL_GENERATED, response);
            return ResponseEntity.ok(responseMessage);
            
        } catch (Exception e) {
            log.error("🟢 [신규 방식] Presigned URL 생성 실패", e);
            throw e;
        }
    }
}
