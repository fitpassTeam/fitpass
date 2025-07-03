package org.example.fitpass.common.s3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import java.util.Date;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3 서비스 테스트")
class S3ServiceTest {

    @Mock
    private AmazonS3 amazonS3;

    @InjectMocks
    private S3Service s3Service;

    private final String TEST_BUCKET_NAME = "test-bucket";
    private final String TEST_FILE_URL = "https://test-bucket.s3.amazonaws.com/test-file.jpg";

    @BeforeEach
    void setUp() {
        // 리플렉션을 사용하여 private 필드 설정
        ReflectionTestUtils.setField(s3Service, "bucketName", TEST_BUCKET_NAME);
    }

    @Test
    @DisplayName("단일 파일 업로드 성공 테스트")
    void uploadSingleFile_Success() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        URL mockUrl = new URL(TEST_FILE_URL);
        when(amazonS3.getUrl(eq(TEST_BUCKET_NAME), anyString())).thenReturn(mockUrl);

        // When
        String result = s3Service.uploadSingleFile(file);

        // Then
        assertThat(result).isEqualTo(TEST_FILE_URL);
        verify(amazonS3, times(1)).putObject(any(PutObjectRequest.class));
        verify(amazonS3, times(1)).getUrl(eq(TEST_BUCKET_NAME), anyString());
    }

    @Test
    @DisplayName("다중 파일 업로드 성공 테스트")
    void uploadFiles_Success() throws Exception {
        // Given
        List<MultipartFile> files = Arrays.asList(
            new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "test1".getBytes()),
            new MockMultipartFile("file2", "test2.png", "image/png", "test2".getBytes())
        );

        URL mockUrl1 = new URL("https://test-bucket.s3.amazonaws.com/test1.jpg");
        URL mockUrl2 = new URL("https://test-bucket.s3.amazonaws.com/test2.png");
        
        when(amazonS3.getUrl(eq(TEST_BUCKET_NAME), anyString()))
            .thenReturn(mockUrl1)
            .thenReturn(mockUrl2);

        // When
        List<String> results = s3Service.uploadFiles(files);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0)).contains("test1.jpg");
        assertThat(results.get(1)).contains("test2.png");
        verify(amazonS3, times(2)).putObject(any(PutObjectRequest.class));
        verify(amazonS3, times(2)).getUrl(eq(TEST_BUCKET_NAME), anyString());
    }

    @Test
    @DisplayName("파일 업로드 실패 - IOException 발생")
    void uploadSingleFile_ThrowsIOException() throws Exception {
        // Given
        MockMultipartFile file = spy(new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        ));

        // Mock에서 IOException 발생하도록 설정
        when(file.getInputStream()).thenThrow(new IOException("파일 읽기 실패"));

        // When & Then
        assertThatThrownBy(() -> s3Service.uploadSingleFile(file))
            .isInstanceOf(BaseException.class)
            .hasFieldOrPropertyWithValue("errorCode", ExceptionCode.S3_UPLOAD_FAIL);

        verify(amazonS3, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    @DisplayName("파일 삭제 성공 테스트")
    void deleteFileFromS3_Success() {
        // Given
        String fileUrl = "https://test-bucket.s3.amazonaws.com/test-file.jpg";

        // When
        s3Service.deleteFileFromS3(fileUrl);

        // Then
        verify(amazonS3, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("파일 삭제 실패 - 예외 발생")
    void deleteFileFromS3_ThrowsException() {
        // Given
        String fileUrl = "https://test-bucket.s3.amazonaws.com/test-file.jpg";
        doThrow(new RuntimeException("삭제 실패")).when(amazonS3).deleteObject(any(DeleteObjectRequest.class));

        // When & Then
        assertThatThrownBy(() -> s3Service.deleteFileFromS3(fileUrl))
            .isInstanceOf(BaseException.class)
            .hasFieldOrPropertyWithValue("errorCode", ExceptionCode.S3_DELETE_FAIL);

        verify(amazonS3, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("URL에서 파일명 추출 테스트 - 특수문자 포함")
    void deleteFileFromS3_WithSpecialCharacters() {
        // Given
        String fileUrl = "https://test-bucket.s3.amazonaws.com/test%20file%20with%20spaces.jpg";

        // When
        s3Service.deleteFileFromS3(fileUrl);

        // Then
        verify(amazonS3, times(1)).deleteObject(argThat(request -> 
            request.getKey().equals("test file with spaces.jpg")
        ));
    }

    @Test
    @DisplayName("확장자가 없는 파일 업로드 테스트")
    void uploadFile_WithoutExtension() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "testfile",
            "application/octet-stream",
            "test content".getBytes()
        );

        URL mockUrl = new URL(TEST_FILE_URL);
        when(amazonS3.getUrl(eq(TEST_BUCKET_NAME), anyString())).thenReturn(mockUrl);

        // When
        String result = s3Service.uploadSingleFile(file);

        // Then
        assertThat(result).isEqualTo(TEST_FILE_URL);
        verify(amazonS3, times(1)).putObject(argThat(request -> 
            !request.getKey().contains(".")
        ));
    }

    @Test
    @DisplayName("빈 파일 리스트 업로드 테스트")
    void uploadFiles_EmptyList() {
        // Given
        List<MultipartFile> files = Arrays.asList();

        // When
        List<String> results = s3Service.uploadFiles(files);

        // Then
        assertThat(results).isEmpty();
        verify(amazonS3, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    @DisplayName("파일 메타데이터 설정 검증")
    void uploadFile_VerifyMetadata() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        URL mockUrl = new URL(TEST_FILE_URL);
        when(amazonS3.getUrl(eq(TEST_BUCKET_NAME), anyString())).thenReturn(mockUrl);

        // When
        s3Service.uploadSingleFile(file);

        // Then
        verify(amazonS3, times(1)).putObject(argThat(request -> {
            assertThat(request.getMetadata().getContentLength()).isEqualTo(file.getSize());
            assertThat(request.getMetadata().getContentType()).isEqualTo("image/jpeg");
            return true;
        }));
    }

    @Test
    @DisplayName("Presigned URL 생성 성공 테스트")
    void generatePresignedUrl_Success() throws Exception {
        // Given
        String filename = "test-image.jpg";
        String contentType = "image/jpeg";
        String expectedPresignedUrl = "https://test-bucket.s3.amazonaws.com/uuid.jpg?presigned=true";
        
        URL mockUrl = new URL(expectedPresignedUrl);
        when(amazonS3.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
            .thenReturn(mockUrl);

        // When
        String result = s3Service.generatePresignedUrl(filename, contentType);

        // Then
        assertThat(result).isEqualTo(expectedPresignedUrl);
        verify(amazonS3, times(1)).generatePresignedUrl(argThat(request -> {
            assertThat(request.getBucketName()).isEqualTo(TEST_BUCKET_NAME);
            assertThat(request.getMethod()).isEqualTo(HttpMethod.PUT);
            assertThat(request.getExpiration()).isNotNull();
            assertThat(request.getContentType()).isEqualTo(contentType);
            // 파일명이 UUID 형태로 변경되었는지 확인
            assertThat(request.getKey()).isNotEqualTo(filename);
            assertThat(request.getKey()).endsWith(".jpg");
            return true;
        }));
    }

    @Test
    @DisplayName("Presigned URL 생성 실패 - 허용되지 않는 파일 타입")
    void generatePresignedUrl_InvalidFileType() {
        // Given
        String filename = "malicious-file.exe";
        String contentType = "application/x-executable";

        // When & Then
        assertThatThrownBy(() -> s3Service.generatePresignedUrl(filename, contentType))
            .isInstanceOf(BaseException.class)
            .hasFieldOrPropertyWithValue("errorCode", ExceptionCode.INVALID_FILE_TYPE);

        verify(amazonS3, never()).generatePresignedUrl(any(GeneratePresignedUrlRequest.class));
    }

    @Test
    @DisplayName("Presigned URL 생성 - 다양한 이미지 파일 타입 테스트")
    void generatePresignedUrl_VariousImageTypes() throws Exception {
        // Given
        String[] allowedFiles = {
            "test.jpg", "test.jpeg", "test.png", "test.gif", 
            "test.webp", "test.bmp", "test.tiff"
        };
        String contentType = "image/jpeg";
        String expectedPresignedUrl = "https://test-bucket.s3.amazonaws.com/uuid.jpg?presigned=true";
        
        URL mockUrl = new URL(expectedPresignedUrl);
        when(amazonS3.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
            .thenReturn(mockUrl);

        // When & Then
        for (String filename : allowedFiles) {
            String result = s3Service.generatePresignedUrl(filename, contentType);
            assertThat(result).isEqualTo(expectedPresignedUrl);
        }

        verify(amazonS3, times(allowedFiles.length))
            .generatePresignedUrl(any(GeneratePresignedUrlRequest.class));
    }

    @Test
    @DisplayName("Presigned URL 생성 - 대소문자 구분 없이 파일 타입 검증")
    void generatePresignedUrl_CaseInsensitiveFileType() throws Exception {
        // Given
        String[] caseVariations = {"test.JPG", "test.Png", "test.GIF", "TEST.JPEG"};
        String contentType = "image/jpeg";
        String expectedPresignedUrl = "https://test-bucket.s3.amazonaws.com/uuid.jpg?presigned=true";
        
        URL mockUrl = new URL(expectedPresignedUrl);
        when(amazonS3.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
            .thenReturn(mockUrl);

        // When & Then
        for (String filename : caseVariations) {
            String result = s3Service.generatePresignedUrl(filename, contentType);
            assertThat(result).isEqualTo(expectedPresignedUrl);
        }

        verify(amazonS3, times(caseVariations.length))
            .generatePresignedUrl(any(GeneratePresignedUrlRequest.class));
    }

    @Test
    @DisplayName("확장자 없는 파일명 → 실패")
    void generatePresignedUrl_NoExtension() {
        // Given
        String filename = "testfile";  // 확장자 없음
        String contentType = "image/jpeg";

        // When & Then
        assertThatThrownBy(() -> s3Service.generatePresignedUrl(filename, contentType))
            .isInstanceOf(BaseException.class)
            .hasMessageContaining("허용되지 않는 파일 형식입니다");
    }

    @Test
    @DisplayName("URL에서 파일명 추출 성공 테스트")
    void extractFileNameFromUrl_Success() {
        // Given
        String presignedUrl = "https://test-bucket.s3.amazonaws.com/uuid-filename.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&expires=123";

        // When
        String result = s3Service.extractFileNameFromUrl(presignedUrl);

        // Then
        assertThat(result).isEqualTo("uuid-filename.jpg");
    }

    @Test
    @DisplayName("URL에서 파일명 추출 - 쿼리 파라미터 없는 경우")
    void extractFileNameFromUrl_NoQueryParams() {
        // Given
        String presignedUrl = "https://test-bucket.s3.amazonaws.com/uuid-filename.jpg";

        // When
        String result = s3Service.extractFileNameFromUrl(presignedUrl);

        // Then
        assertThat(result).isEqualTo("uuid-filename.jpg");
    }

    @Test
    @DisplayName("URL에서 파일명 추출 실패 - 잘못된 URL 형식")
    void extractFileNameFromUrl_InvalidUrl() {
        String invalidUrl = "invalid-url-format";
        String result = s3Service.extractFileNameFromUrl(invalidUrl);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Presigned URL 만료 시간 검증")
    void generatePresignedUrl_ExpirationTime() throws Exception {
        // Given
        String filename = "test.jpg";
        String contentType = "image/jpeg";
        String expectedPresignedUrl = "https://test-bucket.s3.amazonaws.com/uuid.jpg?presigned=true";
        
        URL mockUrl = new URL(expectedPresignedUrl);
        when(amazonS3.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
            .thenReturn(mockUrl);

        long beforeTime = System.currentTimeMillis();

        // When
        s3Service.generatePresignedUrl(filename, contentType);

        long afterTime = System.currentTimeMillis();

        // Then
        verify(amazonS3, times(1)).generatePresignedUrl(argThat(request -> {
            Date expiration = request.getExpiration();
            long expirationTime = expiration.getTime();
            long expectedExpirationTime = beforeTime + (5 * 60 * 1000); // 5분 후
            long maxExpectedTime = afterTime + (5 * 60 * 1000) + 1000; // 테스트 실행 시간 고려
            
            // 만료 시간이 대략 5분 후인지 확인 (약간의 오차 허용)
            assertThat(expirationTime).isBetween(expectedExpirationTime - 1000, maxExpectedTime);
            return true;
        }));
    }
}
