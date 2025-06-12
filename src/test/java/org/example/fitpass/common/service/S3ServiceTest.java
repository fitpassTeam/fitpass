package org.example.fitpass.common.service;


import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.example.fitpass.common.s3.service.S3Service;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@SpringBootTest
@ActiveProfiles("s3test")
class S3ServiceTest {

    @Autowired
    private S3Service s3Service;

    private String uploadedUrl;

    @Test
    @DisplayName("S3에 파일 업로드 성공")
    void uploadFileToS3Test() throws IOException {
        // given
        MockMultipartFile mockFile = getMockFile();

        // when
        uploadedUrl = s3Service.uploadSingleFile(mockFile);

        // then
        assertThat(uploadedUrl).isNotBlank();
        assertThat(uploadedUrl).contains("fitpass-test");
    }

    @Test
    @DisplayName("여러 파일 업로드 성공")
    void uploadMultipleFilesTest() throws IOException {
        // given
        MockMultipartFile mockFile = getMockFile();
        List<MultipartFile> files = Collections.singletonList(mockFile);

        // when
        List<String> urls = s3Service.uploadFiles(files);

        // then
        assertThat(urls).isNotEmpty();
        assertThat(urls.get(0)).startsWith("https://"); // URL 형태 확인
        assertThat(urls.get(0)).endsWith(".jpg"); // 확장자 등 조건 확인
        uploadedUrl = urls.get(0); // 삭제 테스트를 위해 저장
    }

    @Test
    @DisplayName("파일 삭제 성공")
    void deleteFileFromS3Test() throws IOException {
        // given: 먼저 업로드
        MockMultipartFile mockFile = getMockFile();
        String url = s3Service.uploadSingleFile(mockFile);

        // when
        s3Service.deleteFileFromS3(url);

        // then
        // 삭제 성공 로그를 확인하거나, 다시 삭제 시 예외 터지는지 확인 가능
        assertThatNoException().isThrownBy(() -> s3Service.deleteFileFromS3(url));
    }

    private MockMultipartFile getMockFile() throws IOException {
        ClassPathResource resource = new ClassPathResource("test-image.jpg");
        return new MockMultipartFile(
            "file",
            "test-image.jpg",
            "image/jpeg",
            resource.getInputStream()
        );
    }

    @AfterEach
    void cleanUp() {
        if (uploadedUrl != null && !uploadedUrl.isBlank()) {
            try {
                s3Service.deleteFileFromS3(uploadedUrl);
            } catch (Exception ignored) {}
        }
    }
}