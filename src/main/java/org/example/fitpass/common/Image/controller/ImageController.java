package org.example.fitpass.common.Image.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.common.s3.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/image")
public class ImageController {

    private final S3Service s3Service;

    @PostMapping
    public ResponseEntity<ResponseMessage<String>> uploadImage(
        @RequestParam("image") MultipartFile file
    ){
        String uploadedImageUrl = s3Service.uploadSingleFile(file);
        ResponseMessage<String> responseMessage =
            ResponseMessage.success(SuccessCode.GYM_POST_SUCCESS, uploadedImageUrl);
        return ResponseEntity.status(SuccessCode.S3_UPLOAD_SUCCESS.getHttpStatus())
            .body(responseMessage);
    }

    @PostMapping("/multi")
    public ResponseEntity<ResponseMessage<List<String>>> uploadMultiImage(
        @RequestParam("images") List<MultipartFile> files
    ){
        List<String> uploadedImageUrl = s3Service.uploadFiles(files);
        ResponseMessage<List<String>> responseMessage =
            ResponseMessage.success(SuccessCode.GYM_POST_SUCCESS, uploadedImageUrl);
        return ResponseEntity.status(SuccessCode.S3_UPLOAD_SUCCESS.getHttpStatus())
            .body(responseMessage);
    }

    @DeleteMapping
    public ResponseEntity<ResponseMessage<String>> deleteImage(@RequestParam("images") String fileUrl) {
        s3Service.deleteFileFromS3(fileUrl);
        ResponseMessage<String> responseMessage =
            ResponseMessage.success(SuccessCode.S3_DELETE_SUCCESS, "삭제 완료");
        return ResponseEntity.status(SuccessCode.S3_DELETE_SUCCESS.getHttpStatus())
            .body(responseMessage);
    }
}
