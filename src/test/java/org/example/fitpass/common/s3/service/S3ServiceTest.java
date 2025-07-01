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
}
