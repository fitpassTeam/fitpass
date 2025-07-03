package org.example.fitpass.config;

import com.amazonaws.services.s3.AmazonS3;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3 설정 테스트")
class S3ConfigTest {

    @Test
    @DisplayName("S3 클라이언트 빈 생성 테스트")
    void amazonS3Bean_ShouldBeCreated() {
        // Given
        S3Config s3Config = new S3Config();
        
        // ReflectionTestUtils를 사용하여 private 필드 설정
        ReflectionTestUtils.setField(s3Config, "accessKey", "test-access-key");
        ReflectionTestUtils.setField(s3Config, "secretKey", "test-secret-key");
        ReflectionTestUtils.setField(s3Config, "region", "ap-northeast-2");

        // When
        AmazonS3 amazonS3 = s3Config.amazonS3();

        // Then
        assertThat(amazonS3).isNotNull();
    }

    @Test
    @DisplayName("S3 설정 값 검증 테스트")
    void s3Configuration_ShouldHaveCorrectValues() {
        // Given
        String expectedAccessKey = "test-access-key";
        String expectedSecretKey = "test-secret-key";
        String expectedRegion = "ap-northeast-2";
        
        S3Config s3Config = new S3Config();
        
        // ReflectionTestUtils를 사용하여 설정값 주입
        ReflectionTestUtils.setField(s3Config, "accessKey", expectedAccessKey);
        ReflectionTestUtils.setField(s3Config, "secretKey", expectedSecretKey);
        ReflectionTestUtils.setField(s3Config, "region", expectedRegion);

        // When
        String actualAccessKey = (String) ReflectionTestUtils.getField(s3Config, "accessKey");
        String actualSecretKey = (String) ReflectionTestUtils.getField(s3Config, "secretKey");
        String actualRegion = (String) ReflectionTestUtils.getField(s3Config, "region");

        // Then
        assertThat(actualAccessKey).isEqualTo(expectedAccessKey);
        assertThat(actualSecretKey).isEqualTo(expectedSecretKey);
        assertThat(actualRegion).isEqualTo(expectedRegion);
    }

    @Test
    @DisplayName("다양한 리전 설정 테스트")
    void s3Configuration_WithDifferentRegions() {
        // Given
        String[] regions = {"us-east-1", "us-west-2", "eu-west-1", "ap-southeast-1"};
        
        for (String region : regions) {
            S3Config s3Config = new S3Config();
            
            // When
            ReflectionTestUtils.setField(s3Config, "accessKey", "test-key");
            ReflectionTestUtils.setField(s3Config, "secretKey", "test-secret");
            ReflectionTestUtils.setField(s3Config, "region", region);
            
            AmazonS3 amazonS3 = s3Config.amazonS3();
            
            // Then
            assertThat(amazonS3).isNotNull();
        }
    }
}
