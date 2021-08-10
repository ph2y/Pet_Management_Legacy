package com.sju18.petmanagement.domain.community.dto;

import lombok.Data;

import javax.validation.constraints.PositiveOrZero;

@Data
public class DeletePostReqDto {
    @PositiveOrZero(message = "valid.post.id.notNegative")
    private Long id;
}
