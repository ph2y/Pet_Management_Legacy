package com.sju18.petmanagement.domain.community.comment.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

@Data
public class CreateCommentReqDto {
    @PositiveOrZero(message = "valid.post.id.notNegative")
    private Long postId;
    @PositiveOrZero(message = "valid.comment.id.notNegative")
    private Long parentCommentId;
    @NotBlank(message = "valid.comment.contents.blank")
    @Size(max = 10000, message = "valid.comment.contents.size")
    private String contents;
}
