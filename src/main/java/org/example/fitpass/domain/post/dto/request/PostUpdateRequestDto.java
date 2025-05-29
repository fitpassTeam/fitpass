package org.example.fitpass.domain.post.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostUpdateRequestDto {

    private String title;
    private String content;
    private String postImage;



}
