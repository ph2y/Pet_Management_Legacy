package com.sju18.petmanagement.domain.post.dto;

import com.sju18.petmanagement.domain.post.dao.Post;
import lombok.Data;

@Data
public class PostResponseDto {
    private Long id;
    private String subject;
    private String content;
    private String image;
    private String location;

    public PostResponseDto(Post post) {
        this.id = post.getId();
        this.subject = post.getSubject();
        this.content = post.getContent();
        this.image = post.getImage();
        this.location = post.getLocation();
    }
}
