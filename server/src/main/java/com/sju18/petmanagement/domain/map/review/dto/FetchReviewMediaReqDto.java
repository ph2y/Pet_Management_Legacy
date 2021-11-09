package com.sju18.petmanagement.domain.map.review.dto;

import lombok.Data;

import javax.validation.constraints.PositiveOrZero;

@Data
public class FetchReviewMediaReqDto {
    @PositiveOrZero
    Long id;
    @PositiveOrZero
    Integer index;
}
