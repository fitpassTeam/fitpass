package org.example.fitpass.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.BaseEntity;
import org.example.fitpass.domain.post.enums.PostStatus;
import org.example.fitpass.domain.post.enums.PostType;

@Getter
public class PostCreateRequestDto extends BaseEntity {

    @NotBlank(message = "게시물의 status가 없습니다.")
    private final PostStatus status;

    @NotBlank(message = "게시물의 Type이 없습니다.")
    private final PostType postType;

    @NotBlank(message = "게시물의 제목이 없습니다.")
    @Size(max = 50, message = "게시물 제목은 50글자까지 작성 가능 합니다.")
    private final String title;

    @NotBlank(message = "게시물의 내용이 없습니다.")
    private final String content;

    private final String postImage;

    public PostCreateRequestDto(PostStatus status, PostType postType, String title, String content, String postImage) {
        this.status = status;
        this.postType = postType;
        this.title = title;
        this.content = content;
        this.postImage = postImage;
    }
}
