package com.sju18.petmanagement.domain.community.dto;

import lombok.Data;

import javax.validation.constraints.PositiveOrZero;

@Data
public class FetchPostReqDto {
    @PositiveOrZero(message = "valid.post.pageIndex.notNegative")
    private Integer pageIndex;
    @PositiveOrZero(message = "valid.pet.id.notNegative")
    private Long petId;
    @PositiveOrZero(message = "valid.post.id.notNegative")
    private Long id;
}
