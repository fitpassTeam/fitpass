package org.example.fitpass.common.Image.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/images")
@Tag(name = "IMAGE API", description = "S3에 이미지 업로드/삭제하는 API입니다.")
public class ImageController {

    private final S3Service s3Service;

    @Operation(
        summary = "단일 이미지 업로드",
        description = "S3에 단일 이미지를 업로드합니다. 업로드된 이미지의 URL을 반환합니다."
    )
    @PostMapping
    public ResponseEntity<ResponseMessage<String>> uploadImage(
        @Parameter(description = "업로드할 이미지 파일", required = true)
        @RequestParam("image") MultipartFile file
    ) {
        String uploadedImageUrl = s3Service.uploadSingleFile(file);
        ResponseMessage<String> responseMessage =
            ResponseMessage.success(SuccessCode.GYM_POST_SUCCESS, uploadedImageUrl);
        return ResponseEntity.status(SuccessCode.S3_UPLOAD_SUCCESS.getHttpStatus())
            .body(responseMessage);
    }

    @Operation(
        summary = "다중 이미지 업로드",
        description = "S3에 여러 이미지를 업로드합니다. 업로드된 이미지들의 URL 리스트를 반환합니다."
    )
    @PostMapping("/multi")
    public ResponseEntity<ResponseMessage<List<String>>> uploadMultiImage(
        @Parameter(description = "업로드할 이미지 파일 리스트", required = true)
        @RequestParam("images") List<MultipartFile> files
    ) {
        List<String> uploadedImageUrl = s3Service.uploadFiles(files);
        ResponseMessage<List<String>> responseMessage =
            ResponseMessage.success(SuccessCode.GYM_POST_SUCCESS, uploadedImageUrl);
        return ResponseEntity.status(SuccessCode.S3_UPLOAD_SUCCESS.getHttpStatus())
            .body(responseMessage);
    }

    @Operation(
        summary = "이미지 삭제",
        description = "S3에서 이미지 파일을 삭제합니다. 파일 URL을 요청 파라미터로 전달해야 합니다."
    )
    @DeleteMapping
    public ResponseEntity<ResponseMessage<String>> deleteImage(
        @Parameter(description = "삭제할 이미지의 S3 URL", required = true, example = "https://bucket-name.s3.amazonaws.com/image.jpg")
        @RequestParam("images") String fileUrl
    ) {
        s3Service.deleteFileFromS3(fileUrl);
        ResponseMessage<String> responseMessage =
            ResponseMessage.success(SuccessCode.S3_DELETE_SUCCESS, "삭제 완료");
        return ResponseEntity.status(SuccessCode.S3_DELETE_SUCCESS.getHttpStatus())
            .body(responseMessage);
    }
}