package com.sju18.petmanagement.domain.community.dto;

import lombok.Data;

import javax.validation.constraints.PositiveOrZero;

@Data
public class FetchCommentReqDto {
    @PositiveOrZero(message = "valid.comment.pageIndex.notNegative")
    private Integer pageIndex;
    @PositiveOrZero(message = "valid.comment.id.notNegative")
    private Long id;
    @PositiveOrZero(message = "valid.post.id.notNegative")
    private Long postId;
    @PositiveOrZero(message = "valid.comment.id.notNegative")
    private Long parentCommentId;
    @PositiveOrZero(message = "valid.comment.id.notNegative")
    private Long topCommentId;
}
