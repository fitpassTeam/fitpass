package org.example.fitpass.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.example.fitpass.domain.post.enums.PostStatus;
import org.example.fitpass.domain.post.enums.PostType;


public record PostUpdateRequestDto(@NotBlank(message = "게시물의 status가 없습니다.") PostStatus status,
                                   @NotBlank(message = "게시물의 Type이 없습니다.") PostType postType,
                                   @NotBlank(message = "게시물의 제목이 없습니다.") @Size(max = 50, message = "게시물 제목은 50글자까지 작성 가능 합니다.") String title,
                                   @NotBlank(message = "게시물의 내용이 없습니다.") String content,
                                   List<String> postImage) {

    public PostUpdateRequestDto(PostStatus status, PostType postType, String title, String content, List<String> postImage) {
        this.status = status;
        this.postType = postType;
        this.title = title;
        this.content = content;
        this.postImage = postImage;
    }


}
