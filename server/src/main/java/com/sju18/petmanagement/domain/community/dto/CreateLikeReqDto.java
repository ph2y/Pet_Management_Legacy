package com.sju18.petmanagement.domain.community.dto;

import lombok.Data;

import javax.validation.constraints.PositiveOrZero;

@Data
public class CreateLikeReqDto {
    @PositiveOrZero(message = "valid.post.id.notNegative")
    private Long postId;
    @PositiveOrZero(message = "valid.comment.id.notNegative")
    private Long commentId;
}
