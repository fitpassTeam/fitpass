package org.example.fitpass.domain.post.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.example.fitpass.domain.post.enums.PostStatus;
import org.example.fitpass.domain.post.enums.PostType;

import java.util.List;

public record PostCreateRequestDto(
        @Schema(description = "게시물 상태 (ex. 공개, 삭제)", example = "ACTIVE")
        @NotBlank(message = "게시물의 status가 없습니다.")
        PostStatus status,

        @Schema(description = "게시물 타입 (NOTICE, GENERAL)", example = "GENERAL")
        @NotBlank(message = "게시물의 Type이 없습니다.")
        PostType postType,

        @Schema(description = "제목", example = "헬스장 이벤트 안내")
        @NotBlank(message = "게시물의 제목이 없습니다.")
        @Size(max = 50, message = "게시물 제목은 50글자까지 작성 가능 합니다.")
        String title,

        @Schema(description = "내용", example = "이번 주말까지 프로모션이 진행됩니다.")
        @NotBlank(message = "게시물의 내용이 없습니다.")
        String content,

        @Schema(description = "이미지 URL 리스트")
        List<String> postImage
) {

    public PostCreateRequestDto(PostStatus status, PostType postType, String title, String content, List<String> postImage) {
        this.status = status;
        this.postType = postType;
        this.title = title;
        this.content = content;
        this.postImage = postImage;
    }
}
