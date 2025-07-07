//package org.example.fitpass.common.service;
//
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatNoException;
//
//import java.io.IOException;
//import java.util.Collections;
//import java.util.List;
//import org.example.fitpass.common.s3.service.S3Service;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.web.multipart.MultipartFile;
//
//@SpringBootTest
//@ActiveProfiles("s3test")
//@TestPropertySource("classpath:application-s3test.properties")
//class S3ServiceTest {
//
//    @Autowired
//    private S3Service s3Service;
//
//    private String uploadedUrl;
//
//    @Test
//    @DisplayName("S3에 파일 업로드 성공")
//    void uploadFileToS3Test() throws IOException {
//        // given
//        MockMultipartFile mockFile = getMockFile();
//
//        // when
//        uploadedUrl = s3Service.uploadSingleFile(mockFile);
//
//    }
//
//}
//
