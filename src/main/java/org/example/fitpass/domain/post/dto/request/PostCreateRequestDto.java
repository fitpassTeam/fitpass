package org.example.fitpass.domain.post.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.domain.post.enums.PostStatus;
import org.example.fitpass.domain.post.enums.PostType;

@Getter
@NoArgsConstructor
@AllArgsConstructor

public class PostCreateRequestDto {

    private PostStatus status;
    private PostType postType;
    private String title;
    private String content;
    private String postImage;
    private Long userId;
    private Long gymId;

}
