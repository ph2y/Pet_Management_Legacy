package com.sju18.petmanagement.domain.community.post.dto;

import lombok.Data;

import javax.validation.constraints.PositiveOrZero;

@Data
public class FetchPostMediaReqDto {
    @PositiveOrZero
    Long id;
    @PositiveOrZero
    Integer index;
}
