package org.example.fitpass.domain.post.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.example.fitpass.domain.post.enums.PostStatus;
import org.example.fitpass.domain.post.enums.PostType;


@Schema(description = "게시물 수정 요청 DTO")
public record PostUpdateRequestDto(

    @Schema(description = "게시물 상태 (공개/비공개)", example = "PUBLIC")
    @NotBlank(message = "게시물의 status가 없습니다.")
    PostStatus status,

    @Schema(description = "게시물 유형 (예: 공지사항, 일반글)", example = "NORMAL")
    @NotBlank(message = "게시물의 Type이 없습니다.")
    PostType postType,

    @Schema(description = "게시물 제목 (최대 50자)", example = "오늘의 운동 후기")
    @NotBlank(message = "게시물의 제목이 없습니다.")
    @Size(max = 50, message = "게시물 제목은 50글자까지 작성 가능 합니다.")
    String title,

    @Schema(description = "게시물 내용", example = "오늘은 하체 위주로 운동을 했습니다.")
    @NotBlank(message = "게시물의 내용이 없습니다.")
    String content,

    @Schema(description = "게시물 이미지 URL 목록", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
    List<String> postImage

) {
    public PostUpdateRequestDto(PostStatus status, PostType postType, String title, String content, List<String> postImage) {
        this.status = status;
        this.postType = postType;
        this.title = title;
        this.content = content;
        this.postImage = postImage;
    }
}
