package com.sju18.petmanagement.domain.community.dto;

import lombok.Data;

import javax.validation.constraints.PositiveOrZero;

@Data
public class DeleteCommentReqDto {
    @PositiveOrZero(message = "valid.comment.id.notNegative")
    private Long id;
}
