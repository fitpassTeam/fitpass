package org.example.fitpass.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.domain.post.enums.PostStatus;
import org.example.fitpass.domain.post.enums.PostType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostUpdateRequestDto {

    @NotBlank(message = "게시물의 status가 없습니다.")
    private PostStatus status;

    @NotBlank(message = "게시물의 Type이 없습니다.")
    private PostType postType;

    @NotBlank(message = "게시물의 제목이 없습니다.")
    @Size(max = 50, message = "게시물 제목은 50글자까지 작성 가능 합니다.")
    private String title;

    @NotBlank(message = "게시물의 내용이 없습니다.")
    private String content;

    private String postImage;

}
