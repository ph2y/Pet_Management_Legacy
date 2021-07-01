package com.sju18.petmanagement.domain.post.dto;

import com.sju18.petmanagement.domain.post.dao.Post;
import lombok.Builder;
import lombok.Data;

@Data
public class PostCreateRequestDto {
    private String subject;
    private String content;
    private String image;
    private String location;

    @Builder
    public PostCreateRequestDto(String subject, String content, String image, String location) {
        this.subject = subject;
        this.content = content;
        this.image = image;
        this.location = location;
    }

    public Post toEntity(String username) {
        return Post.builder()
                .username(username)
                .subject(subject)
                .content(content)
                .image(image)
                .location(location)
                .build();
    }
}
