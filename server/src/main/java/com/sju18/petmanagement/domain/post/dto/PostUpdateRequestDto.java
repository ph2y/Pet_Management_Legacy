package com.sju18.petmanagement.domain.post.dto;

import lombok.Data;

@Data
public class PostUpdateRequestDto {
    private Long id;
    private String subject;
    private String content;
    private String image;
    private String location;
}
